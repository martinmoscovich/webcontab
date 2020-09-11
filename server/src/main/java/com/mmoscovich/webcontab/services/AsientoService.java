package com.mmoscovich.webcontab.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.dao.AsientoRepository;
import com.mmoscovich.webcontab.dao.CuentaRepository;
import com.mmoscovich.webcontab.dao.InformeRepository;
import com.mmoscovich.webcontab.dao.helper.QueryBalance.FiltroBalance;
import com.mmoscovich.webcontab.dto.informes.BalanceCuenta;
import com.mmoscovich.webcontab.exception.EjercicioFechaInvalidaException;
import com.mmoscovich.webcontab.exception.EjercicioFinalizadoException;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.model.Asiento;
import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Imputacion;
import com.mmoscovich.webcontab.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de Asientos.
 *
 */
@Slf4j
@Service
public class AsientoService {

	@Inject
	private AsientoRepository asientoDao;

	@Inject
	private ImputacionService imputacionService;
	
	@Inject
	private InformeRepository informeDao; 
	
	@Inject
	private CuentaService cuentaService;
	
	@Inject
	private CategoriaService catService;
	
	@Inject
	private CuentaRepository cuentaDao;

	/**
	 * Devuelve una pagina de asientos del ejercicio especificado, dentro del periodo indicado.
	 * <p>Permite opcionalmente traer las imputaciones de cada asiento.</p>
	 * @param ejercicio
	 * @param desde fecha a partir de la cual buscar
	 * @param hasta fecha hasta la cual buscar
	 * @param incluirImputaciones indica si se deben buscar las imputaciones de cada asiento
	 * @param page datos de paginacion
	 * @return
	 */
	@Transactional(readOnly = true)
	public Page<Asiento> findByPeriodo(Ejercicio ejercicio, LocalDate desde, LocalDate hasta, boolean incluirImputaciones, Pageable page) {
		if(desde == null) desde = ejercicio.getInicio();
		if(hasta == null) hasta = ejercicio.getFinalizacion();
		Page<Asiento> pagina =  asientoDao.findByPeriodo(ejercicio, desde, hasta, page);
		
		if(incluirImputaciones) this.addImputaciones(pagina.getContent());
		return pagina;
	}
	
	/**
	 * Devuelve una pagina de asientos del ejercicio especificado, cuyos numeros estan dentro del rango indicado.
	 * <p>Permite opcionalmente traer las imputaciones de cada asiento.</p>
	 * @param ejercicio
	 * @param min numero minimo de asiento
	 * @param max numero maximo de asiento
	 * @param incluirImputaciones indica si se deben buscar las imputaciones de cada asiento
	 * @param page datos de paginacion
	 * @return
	 */
	@Transactional(readOnly = true)
	public Page<Asiento> findByNumeros(Ejercicio ejercicio, Short min, Short max, boolean incluirImputaciones, Pageable page) {
		if(min == null) min = 0;
		if(max == null) max = Short.MAX_VALUE;
		Page<Asiento> pagina =  asientoDao.findByNumeros(ejercicio, min, max, page);
		
		if(incluirImputaciones) this.addImputaciones(pagina.getContent());
		return pagina;
	}

	/**
	 * Busca y agrega las imputaciones a cada asiento del conjunto especificado.
	 * <p>Esta optimizado para buscar las imputaciones de todos los asientos con una sola query.</p>
	 * @param asientos
	 */
	private void addImputaciones(Collection<Asiento> asientos) {
		// Busca las imputaciones de TODOS los asientos
		List<Imputacion> imputaciones = imputacionService.findByAsientos(asientos);
		
		// Itera los asientos y agrega a cada uno las imputaciones que le corresponden
		for(Asiento a : asientos) {
			a.setImputaciones(CollectionUtils.filter(imputaciones, i -> i.perteneceA(a)));
		}
	}

	/**
	 * Obtiene un asiento por id o lanza un {@link EntityNotFoundException} si no existe
	 * @param ejercicio ejercicio en el cual se busca el asiento
	 * @param id id del asiento
	 * @param includeImputaciones indica si se deben includir las imputaciones
	 * @return el asiento encontrado
	 * @throws EntityNotFoundException si no existe asiento con ese id para ese ejercicio
	 */
	public Asiento getByIdOrThrow(Ejercicio ejercicio, Long id, boolean includeImputaciones) throws EntityNotFoundException {
		Asiento asiento = (includeImputaciones ? asientoDao.findByIdWithImputaciones(id) : asientoDao.findById(id))
				.orElseThrow(() -> new EntityNotFoundException(Asiento.class, id));
	
		// Si se encuentra el asiento pero no pertenece al ejercicio, se lanza not found tambien
		if(!asiento.perteneceA(ejercicio)) throw new EntityNotFoundException("El asiento no pertenece al ejercicio seleccionado");

		return asiento;
	}

	/**
	 * Obtiene el numero del proximo asiento del ejercicio.
	 * <br>Para esto, obtiene el maximo numero y le suma 1.
	 * @param ejercicio
	 * @return
	 */
	public Short getProximoNumero(Ejercicio ejercicio) {
		return (short) (asientoDao.getUltimoNumeroAsiento(ejercicio) + 1);
	}
	
	/**
	 * Obtiene la ultima fecha de asiento cargada en el ejercicio.
	 * * <p>O sea, la fecha mas tardia para un asiento.
	 * <br>No necesariamente es el ultimo asiento cargado, ya que se pueden cargar desordenados.</p>
	 * 
	 * @param ejercicio ejercicio donde se busca el asiento
	 * @return optional con la ultima fecha de asiento del ejercicio o vacio si no hay asientos
	 */
	public Optional<LocalDate> getUltimaFechaAsiento(Ejercicio ejercicio) {
		return asientoDao.getUltimaFechaAsiento(ejercicio);
	}
	
	/**
	 * Crea un nuevo asiento con los datos e imputaciones indicadas.
	 * <p>Realiza las validaciones de ejercicio, asiento e imputaciones.
	 * <br>Si todo es valido, realiza las asociaciones de la entidades asiento -> imputacion -> cuenta.
	 * <br>Le asigna el numero de asiento correspondiente.
	 * <br>Persiste.
	 * @param asiento
	 * @param ejercicio
	 * @return
	 * @throws InvalidRequestException si falla la validacion
	 * @throws EjercicioFinalizadoException si el ejercicio ya termino
	 * @throws EjercicioFechaInvalidaException si la fecha no esta dentro del ejercicio
	 */
	@Transactional
	public Asiento crear(Asiento asiento, Ejercicio ejercicio)
			throws InvalidRequestException, EjercicioFinalizadoException, EjercicioFechaInvalidaException {
		// No se puede crear un asiento luego de finalizado el ejercicio
		ejercicio.validateActivo();

		asiento.setId(null);
		asiento.setEjercicio(ejercicio);

		// Se chequean los datos basicos del asiento (pero no los saldos aun)
		asiento.validar(false);

		// Se asocian las imputaciones a las cuentas persistidas
		List<Imputacion> imputaciones = asiento.getImputaciones();
		for (Imputacion i : imputaciones) imputacionService.asociarImputacion(i, asiento);

		// Una vez cargadas las imputaciones, con sus cuentas (y por lo tanto la moneda)
		// se puede validar el saldo
		asiento.validarSaldo();

		// Se le asigna el proximo numero disponible en el ejercicio
		asiento.setNumero(this.getProximoNumero(ejercicio));
		
		// Se persiste el asiento y sus imputaciones
		return this.persistir(asiento);
	}
	
	/**
	 * Actualiza un asiento y sus imputaciones.
	 * @param ejercicio
	 * @param asiento
	 * @return asiento actualizado (con sus imputaciones actualizadas)
	 * @throws EntityNotFoundException si no existe el asiento
	 * @throws InvalidRequestException si hay algun error de validacion
	 * @throws EjercicioFinalizadoException si el ejercicio ya finalizo (no se puede modificar)
	 * @throws EjercicioFechaInvalidaException si la fecha del asiento no esta dentro del ejercicio
	 */
	@Transactional
	public Asiento actualizar(Ejercicio ejercicio, Asiento asiento) throws EntityNotFoundException, InvalidRequestException, EjercicioFinalizadoException, EjercicioFechaInvalidaException {
		Asiento existing = this.getByIdOrThrow(ejercicio, asiento.getId(), true);

		// No se puede modificar un asiento luego de finalizado el ejercicio
		existing.getEjercicio().validateActivo();

		// Se actualizan los datos del asiento
		existing.setFecha(asiento.getFecha());
		existing.setDetalle(asiento.getDetalle());

		// Se actualizan las imputaciones: se agregan las nuevas, se actualizan las existentes
		// y se obtienen las que hay que borrar
		List<Imputacion> imputacionesABorrar = imputacionService.actualizarImputaciones(existing, asiento.getImputaciones());

		// Se valida el asiento incluyendo el saldo, ya que estan cargadas las
		// imputaciones con sus cuentas y monedas
		existing.validar(true);

		// Se eliminan las imputaciones que se quitaron
		imputacionService.eliminar(imputacionesABorrar);

		// Se persiste el asiento y sus imputaciones (nuevas y actualizadas)
		return this.persistir(existing);
	}

	/**
	 * Elimina un asiento y todas sus imputaciones (hace cascade manual).
	 * <p>Ejecuta 2 queries en total: una para el asiento y una para TODAS sus imputaciones.</p>
	 * @param ejercicio
	 * @param id
	 * @throws EntityNotFoundException si no existe el asiento
	 * @throws EjercicioFinalizadoException si el ejercicio ya finalizo (no se puede modificar)
	 */
	@Transactional
	public void eliminar(Ejercicio ejercicio, Long id) throws EntityNotFoundException, EjercicioFinalizadoException {
		Asiento asiento = this.getByIdOrThrow(ejercicio, id, false);

		// No se puede eliminar un asiento luego de finalizado el ejercicio
		asiento.getEjercicio().validateActivo();
		
		// Elimina las imputaciones del asiento
		imputacionService.eliminarByAsiento(asiento);
		
		// Elimina el asiento
		asientoDao.delete(asiento);
	}
	
	/**
	 * Elimina los asientos indicados y sus imputaciones.
	 * <p>Es similar a ejecutar {@link #eliminar(Ejercicio, Long)} para cada id, pero optimizado.</p>
	 * <p>Se ejecuta una query por cada asiento para eliminar sus imputaciones, pero todos los asientos se eliminan en una sola.</p>
	 * @param ejercicio
	 * @param ids lista de ids de asientos
	 * @throws EjercicioFinalizadoException si el ejercicio esta finalizado
	 */
	@Transactional
	public void eliminar(Ejercicio ejercicio, Set<Long> ids) throws EjercicioFinalizadoException {
		// No se puede eliminar un asiento luego de finalizado el ejercicio
		ejercicio.validateActivo();
		
		// Se eliminan las imputaciones de todos los asientos
		for(Long id : ids) imputacionService.eliminarByAsiento(asientoDao.getOne(id));
		
		// Se eliminan los asientos
		asientoDao.deleteByIds(ejercicio, ids);
	}
	
	/**
	 * Elimina todos los asientos de un ejercicio y sus imputaciones (hace cascade manual).
	 * <p>Se usa cuando se desea eliminar un ejercicio.</p>
	 * <p>Ejecuta solo 2 queries en total: una para TODOS los asientos y otra para TODAS las imputaciones.</p>
	 * @param ejercicio
	 * @throws EntityNotFoundException
	 * @throws EjercicioFinalizadoException
	 */
	@Transactional
	public void eliminarTodos(Ejercicio ejercicio) {
		imputacionService.eliminarByEjercicio(ejercicio);
		asientoDao.deleteByEjercicio(ejercicio);
	}
	
	/**
	 * Crea, calcula y persiste el asiento de apertura de un ejercicio.
	 * <p>El asiento de cierre lleva todos los saldos a cero. El objetivo del asiento de apertura es volver dichos saldos
	 * al estado anterior al cierre.</p>
	 * @param ejercicio nuevo ejercicio
	 * @param imputacionesCierreAnterior imputaciones de cierre del ejercicio anterior, a partir de las cuales se calculan las de este asiento.
	 * @return el asiento persistido
	 */
	@Transactional
	public Asiento crearApertura(Ejercicio ejercicio, List<Imputacion> imputacionesCierreAnterior) {
		log.info("Creando asiento de apertura del ejercicio de la organizacion {} y periodo [{} - {}]", ejercicio.getOrganizacion().getNombre(), ejercicio.getInicio(), ejercicio.getFinalizacion());
		
		// El nuevo asiento sera el 1, con fecha igual a la de inicio del ejercicio
		Asiento apertura = new Asiento(ejercicio, (short)1, ejercicio.getInicio(), "Apertura de Libros", null);
		
		// Este asiento contiene imputaciones inversas a las del cierre anterior, para volver los saldos del balance
		// al estado anterior a cerrar el ejercicio
		for(Imputacion impCierre : imputacionesCierreAnterior) {
			apertura.agregarImputacion(impCierre.crearInversa("Apertura de Libros"));
		}
		return this.persistir(apertura);
	}

	/**
	 * Simula como seria el asiento de cierre de un ejercicio activo.
	 * <p>Se utiliza para crear el asiento de apertura del siguiente ejercicio sin la necesidad de que el actual este finalizado.</p> 
	 * <p>Las diferencias con {@link #crearCierre(Ejercicio, Short)} son que este
	 * metodo <b>NO</b> modifica el ejercicio, solo calcula el asiento en memoria, y ademas asume que no existe el asiento de refundicion de resultados, por lo que lo calcula internamente.
	 * <br>Termina generando las mismas imputaciones que {@link #crearCierre(Ejercicio, Short)} pero sin modificar la base de datos.</p>
	 * @param ejercicio
	 * @return
	 */
	@Transactional(readOnly = true)
	public Asiento simularCierre(Ejercicio ejercicio) {
		
		// Se buscan las cuentas de balanceo y si no existen, no se puede continuar
		List<Cuenta> balanceadoras = cuentaService.findCuentasQueBalanceanResultados(ejercicio.getOrganizacion());
		if(balanceadoras.isEmpty()) throw new InvalidRequestException("No existen cuentas que balanceen los resultados");
		
		// Se buscan el saldo de las cuentas de resultados para cada moneda.
		// Estas tienen que ir a las cuentas que balancean los resultados
		List<Categoria> categoriasResultado = catService.findCategoriasDeResultados(ejercicio.getOrganizacion());
		FiltroBalance filtroTotales = new FiltroBalance(ejercicio);
		filtroTotales.soloEnCategorias(categoriasResultado);
		filtroTotales.setPeriodo(ejercicio.getInicio(), ejercicio.getFinalizacion());
		Map<Long, BigDecimal> saldos = informeDao.getBalanceTotales(filtroTotales);
		
		// Se crean las imputaciones de cierre de las cuentas que balancean
		List<Imputacion> imputaciones = this.crearImputacionesDeBalanceoDeResultados(balanceadoras, saldos);
		
		// Se buscan los saldos de las cuentas que NO son de resultados
		// Las de resultados, sabemos que seran cero, no van a estar en el asiento de cierre
		FiltroBalance filtro = new FiltroBalance(ejercicio);
		filtro.excluirCategorias(categoriasResultado);
		Stream<BalanceCuenta> balance = informeDao.streamBalance(filtro);
		
		balance.forEach(b -> {
			// Se busca si ya existe una imputacion en el asiento para la cuenta
			Imputacion imp = imputaciones.stream()
					.filter(i -> i.getCuenta().getId().equals(b.getId()))
					.findFirst()
					.orElse(null);
			
			if(imp != null) {
				// Si encontro la imputacion, es de resultados, se debe acumular con lo calculado arriba
				imp.setImporte(imp.getImporte().add(b.getSaldo().negate()));
			} else {
				// Si no la encontro, crear la imputacion
				// Tiene que ser inverso: lleva el balance a cero
				imputaciones.add(new Imputacion(cuentaDao.getOne(b.getId()), b.getSaldo().negate(), "Cierre de Libros"));
			}
		});
		
		log.debug("Se encontraron {} cuentas de resultados con saldo <> 0 en el ejercicio", imputaciones.size());
		
		// Se ordena por cuenta
		// TODO Deberia ser por "c.orden"
		imputaciones.sort((i, i2) -> i.getCuenta().getId().compareTo(i2.getCuenta().getId()));
		
		return new Asiento(ejercicio, null, ejercicio.getFinalizacion(), "Cierre de Libros", imputaciones);
	}

	/**
	 * Calcula, crea y persiste el asiento de cierre del ejercicio.
	 * <p>Este asiento genera que los saldos de todas las cuentas queden en cero.</p>
	 * <p>Este metodo modifica el ejercicio y debe llamarse <b>despues</b> de crear el asiento de refundicion de resultados.</p>
	 * @param ejercicio
	 * @param numeroAsiento numero de asiento a utilizar 
	 * @return el asiento persistido
	 */
	@Transactional
	public Asiento crearCierre(Ejercicio ejercicio, Short numeroAsiento) {
		log.debug("Creando Asiento de Cierre de ejercicio para ejercicio {}", ejercicio);
		
		// El asiento tendra la fecha de finalizacion del ejercicio
		Asiento asiento = new Asiento(ejercicio, numeroAsiento, ejercicio.getFinalizacion(), "Cierre de Libros", null);
		
		// Se obtiene el saldo de todas las cuentas en el ejercicio
		Stream<BalanceCuenta> balance = informeDao.streamBalance(new FiltroBalance(ejercicio));
		balance.forEach(b -> {
			// Tiene que ser inverso: lleva el balance a cero
			asiento.agregarImputacion(new Imputacion(cuentaDao.getOne(b.getId()), b.getSaldo().negate(), "Cierre de Libros"));
		});
		
		Asiento cierre = this.persistir(asiento);
		if(log.isDebugEnabled()) {
			log.debug("Se creo el asiento de Cierre de ejercicio con numero {} con {} imputaciones", cierre.getNumero(), cierre.getImputaciones().size());
		}
		
		return cierre;
	}
	
	/**
	 * Calcula, crea y persiste el asiento de refundicion de cuentas de resultado, en caso de ser necesario.
	 * <p>Este asiento lleva a cero el saldo de todas las cuentas descendientes de categorias de resultados (Ej: INGRESOS y EGRESOS).</p>
	 * <p>Para que el asiento tenga saldo 0, los saldos resultantes se asignan (inversos) a las cuentas que fueron
	 * designadas como "balanceadoras de resultados" (ej: RESULTADOS DEL EJERCICIO).
	 * <br>Debe haber una por moneda.</p>
	 * <p>Si las cuentas de resultado ya tienen saldo 0, no se genera ninguna imputacion y por lo tanto, no tiene sentido
	 * crear este asiento. En ese caso <b>no se persiste</b> y se devuelve un opcional vacio.</p>
	 * @param ejercicio
	 * @param numAsiento
	 * @return el asiento persistido si fue creado o un optional vacio en caso contrario.
	 */
	@Transactional
	public Optional<Asiento> crearRefundicion(Ejercicio ejercicio, Short numAsiento) {
		log.debug("Calculando Asiento de refundicion de cuentas de resultado para ejercicio {}", ejercicio);
		
		// Se obtienen las cuentas que balancean resultados. Falla si no existen
		List<Cuenta> balanceadoras = cuentaService.findCuentasQueBalanceanResultados(ejercicio.getOrganizacion());
		if(balanceadoras.isEmpty()) throw new InvalidRequestException("No existen cuentas que balanceen los resultados");
		
		// Se crea el asiento con la fecha de finalizacion del ejercicio
		Asiento asiento = new Asiento(ejercicio, numAsiento, ejercicio.getFinalizacion(), "Refundición de cuentas de resultado", null);
		
		
		// Se buscan los saldos de las cuentas de resultados
		List<Categoria> categoriasResultado = catService.findCategoriasDeResultados(ejercicio.getOrganizacion());
		FiltroBalance filtro = new FiltroBalance(ejercicio);
		filtro.soloEnCategorias(categoriasResultado);
		Stream<BalanceCuenta> balance = informeDao.streamBalance(filtro);
		
		// Para cada saldo, se crea una imputacion que lo lleva a cero (inversa)
		balance.forEach(b -> {
			asiento.agregarImputacion(new Imputacion(cuentaDao.getOne(b.getId()), b.getSaldo().negate(), "Refundición de cuentas de resultado"));
		});
		
		Integer cantCuentas = asiento.getImputaciones().size();
		
		// Aqui ya estan las imputaciones que hacen 0 los saldos de las cuentas de resultados
		// Pero el asiento no tendra saldo 0, entonces se crean imputaciones a las cuentas "balanceadoras" (una por moneda) para
		// llevar el asiento a saldo 0.
		Map<Long, BigDecimal> saldos = asiento.getSaldos();
		asiento.agregarImputaciones(this.crearImputacionesDeBalanceoDeResultados(balanceadoras, asiento.getSaldos()));
		
		if(asiento.getImputaciones().isEmpty()) {
			log.warn("Las cuentas de resultado tienen saldo en cero, no se genera el asiento de refundicion de cuentas de resultado");
			return Optional.empty();

		} else {
			Asiento refundicion = this.persistir(asiento);
			if(log.isDebugEnabled()) {
				log.debug("Se encontraron {} cuentas de resultados con saldo <> 0 en el ejercicio y {} monedas distintas", cantCuentas, saldos.size());
				log.debug("Se creo el asiento de Refundicion de cuentas de resultado con numero {} y {} imputaciones", asiento.getNumero(), cantCuentas + saldos.size());
			}
			return Optional.of(refundicion);
		}
	}
	
	/**
	 * Dado un conjunto de cuentas balanceadoras (una por moneda) y un conjunto de saldos (uno por moneda),
	 * se crean las imputaciones en dichas cuentas para llevar los saldos a 0 (o sea se invierten).
	 * @param cuentasBalanceadoras
	 * @param saldos mapa cuya clave es el id de moneda y value es el saldo
	 * @return imputaciones que cancelan dichos saldos
	 */
	private List<Imputacion> crearImputacionesDeBalanceoDeResultados(List<Cuenta> cuentasBalanceadoras, Map<Long, BigDecimal> saldos) {
		List<Imputacion> result = new ArrayList<>();
		
		// Por cada saldo (uno por moneda)
		for(Entry<Long, BigDecimal> entry : saldos.entrySet()) {
			
			// Se busca la cuenta balanceadora para dicha moneda (falla si no existe)
			Cuenta balanceadora = cuentasBalanceadoras.stream().filter(c -> c.getMoneda().getId().equals(entry.getKey()))
					.findFirst().orElseThrow(() -> new InvalidRequestException("No existe cuenta que balancee resultados para la moneda con id " + entry.getKey()));
					
			BigDecimal saldo = entry.getValue();
			
			// Se crea la imputacion en dicha cuenta que lleva el saldo a 0
			result.add(new Imputacion(balanceadora, saldo.negate(), "Refundición de cuentas de resultado"));
		}
		return result;
	}
	
	/**
	 * Guarda un asiento y todas sus imputaciones.
	 * <p>Realiza el save en cascada de manera manual.</p>
	 * @param asiento
	 * @return
	 */
	private Asiento persistir(Asiento asiento) {
		if(asiento.getId() == null) {
			// Es un alta, primero se guarda el asiento y luego sus imputaciones
			// Al reves no funciona porque las imputaciones necesitan un asiento persistido
			asiento = asientoDao.save(asiento);
			imputacionService.persistir(asiento.getImputaciones());
		} else {
			// Es una modificacion. Se deben guardar primero las imputaciones, para que no haga refresh de la lista de imputaciones y
			// pierda los datos nuevos
			imputacionService.persistir(asiento.getImputaciones());
			asiento = asientoDao.save(asiento);
		}
		return asiento;
	}
}
