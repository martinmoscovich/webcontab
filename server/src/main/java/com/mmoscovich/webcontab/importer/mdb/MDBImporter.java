package com.mmoscovich.webcontab.importer.mdb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmoscovich.webcontab.dao.CuentaBaseRepository;
import com.mmoscovich.webcontab.dao.EjercicioRepository;
import com.mmoscovich.webcontab.dao.MonedaRepository;
import com.mmoscovich.webcontab.dao.OrganizacionRepository;
import com.mmoscovich.webcontab.exception.EntityNotFoundException;
import com.mmoscovich.webcontab.exception.InvalidRequestException;
import com.mmoscovich.webcontab.exception.ServerException;
import com.mmoscovich.webcontab.exception.WebContabException;
import com.mmoscovich.webcontab.importer.ImportTask;
import com.mmoscovich.webcontab.importer.ImportTask.ImportStatus;
import com.mmoscovich.webcontab.importer.ImportTask.ImportTaskSummary;
import com.mmoscovich.webcontab.importer.Importer;
import com.mmoscovich.webcontab.importer.mdb.AccessReader.ImportedAsiento;
import com.mmoscovich.webcontab.importer.mdb.AccessReader.ImportedCuenta;
import com.mmoscovich.webcontab.importer.mdb.AccessReader.ImportedEjercicio;
import com.mmoscovich.webcontab.importer.mdb.AccessReader.ImportedMovimiento;
import com.mmoscovich.webcontab.model.Asiento;
import com.mmoscovich.webcontab.model.Categoria;
import com.mmoscovich.webcontab.model.Cuenta;
import com.mmoscovich.webcontab.model.CuentaBase;
import com.mmoscovich.webcontab.model.Ejercicio;
import com.mmoscovich.webcontab.model.Imputacion;
import com.mmoscovich.webcontab.model.Moneda;
import com.mmoscovich.webcontab.model.Organizacion;
import com.mmoscovich.webcontab.services.AsientoService;
import com.mmoscovich.webcontab.services.CategoriaService;
import com.mmoscovich.webcontab.services.CuentaService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementacion de {@link Importer} que lee los datos de un MDB de WinContab.
 */
@Slf4j
@Service
public class MDBImporter implements Importer {
	
	public static enum CuentaImportStrategy { CODIGO, NIVEL };
	
	@Inject
	private EjercicioRepository ejDao;
	@Inject
	private OrganizacionRepository orgDao;
	@Inject
	private CuentaBaseRepository cuentaDao;
	@Inject
	private CuentaService cuentaService;
	@Inject
	private CategoriaService categoriaService;
	@Inject
	private AsientoService asientoService;
	@Inject
	private MonedaRepository monedaDao;
	
	/** Directorio donde se almacenan los MDB a importar */
	@Value("${webcontab.importer.backupDir}")
	private Path tmpDir;
	
	@PostConstruct
	public void createTmpDir() throws IOException {
		// Se crean los directorios necesarios
		if(Files.notExists(tmpDir)) {
			log.info("No existe el directorio de temporal de importacion, se crea");
			Files.createDirectories(tmpDir);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public ImportTask read(Path file, String fileName) throws InvalidRequestException {
		try {
			
			ImportedEjercicio importedEjercicio;
			// Lee la organizacion y el ejercicio del MDB
			try(AccessReader reader = this.getReader(file)) {
				importedEjercicio = reader.getEjercicio();
			}
			if(importedEjercicio == null) throw new InvalidRequestException("La base cargada no posee ejercicio");
			
			// Se crea u obtiene la organizacion en base de su CUIT y Nombre
			Organizacion org = this.validarYCrearOrg(importedEjercicio.cuit, importedEjercicio.razonSocial, false);
			
			// Se crea u obtiene el ejercicio
			Ejercicio ejercicio = this.validarYCrearEjercicio(org, importedEjercicio.inicio, importedEjercicio.finalizacion, false);
			
			// Se genera un ID para la tarea
			UUID uuid = UUID.randomUUID();
			
			// Ya validado, se mueve el archivo a su ubicacion
			String tmpName = FilenameUtils.getBaseName(fileName) + "-" + uuid + "." + FilenameUtils.getExtension(fileName);
			Path tmpFile = this.tmpDir.resolve(tmpName);
			Files.move(file, tmpFile);
			
			ImportTask task = new ImportTask(uuid, tmpFile, org, ejercicio);
			
			return task;
			
		} catch(WebContabException e) {
			throw e;
		} catch(Exception e) {
			throw new ServerException("Error al cargar archivo " + file.toString(), e);
		}
	}
	
	/**
	 * Obtiene o crea la organizacion en base al cuit y nombre.
	 * <p>
	 * La logica es la siguiente: 
	 * <br>Si existe una organizacion con el mismo CUIT, se devuelve esa.
	 * <br>Si existe una con distinto CUIT pero el mismo nombre, se lanza excepcion.
	 * <br>Si no existe ninguna con ese CUIT o nombre, se crea.
	 * @param cuit
	 * @param nombre
	 * @param persist indica si al crear se debe persistir o solo devolver
	 * @return
	 * @throws InvalidRequestException
	 */
	private Organizacion validarYCrearOrg(String cuit, String nombre, boolean persist) throws InvalidRequestException {
		// ORGANIZACION
		// Debe existir una con ese CUIT y ningun otra debe tener el mismo nombre
		List<Organizacion> orgs = orgDao.findByCuitOrNombre(cuit, nombre);
		
		if(orgs.isEmpty()) {
			// No existe, se crea
			Organizacion org  = new Organizacion(cuit, nombre);
			return persist ? orgDao.save(org) : org;
			
		} else if(orgs.size() == 1 && orgs.get(0).getCuit().equals(cuit)) {
			// Existe una sola y tiene ese CUIT
			return orgs.get(0);
		} else {
			// Existe una con el mismo nombre pero distinto CUIT
			throw new InvalidRequestException("La organizacion importada posee el mismo nombre que otra pero diferente CUIT (" + cuit + " - " + nombre + ")");
		}
	}
	/**
	 * Obtiene o crea el ejercicio en base a las fechas de inicio y fin.
	 * <p>
	 * La logica es la siguiente: 
	 * <br>Si la organizacion no existe, el ejercicio es nuevo y se crea.
	 * <br>Si ninguna ejercicio existente se solapa con las fechas, se crea.
	 * <br>Si se solapa mas de uno, se lanza excepcion.
	 * <br>Si se solapa uno pero no tiene exactamente las mismas fechas, se lanza excepcion.
	 * <br>Si existe un ejercicio con exactamente las mismas fechas, se usa ese.
	 * @param org
	 * @param inicio
	 * @param finalizacion
	 * @param persist indica si al crear se debe persistir o solo devolver
	 * @return
	 */
	private Ejercicio validarYCrearEjercicio(Organizacion org, LocalDate inicio, LocalDate finalizacion, boolean persist) {
		if(inicio == null) throw new InvalidRequestException("El ejercicio de la base cargada no tiene fecha de inicio");
		if(finalizacion == null) throw new InvalidRequestException("El ejercicio de la base cargada no tiene fecha de fin");

		// Si la organizacion ya existia, puede que el ejercicio tambien
		if(org.getId() != null) {
			// Se obtienen los ejercicios de la base que solapan con el que se quiere importar
			List<Ejercicio> ejercicios = ejDao.findEjerciciosQueSolapan(org, inicio, finalizacion);
		
			// Solo sera valido si NO solapa ninguno (es nuevo) o si solapa uno que coincida exactamente (se usa ese)
			if(
					ejercicios.size() == 1 && 
					ejercicios.get(0).getInicio().equals(inicio) && 
					ejercicios.get(0).getFinalizacion().equals(finalizacion)
			) {
				return ejercicios.get(0);
				
			} else if(!ejercicios.isEmpty()) {
				// El ejercicio no es valido. Si se pidio persistir, se lanza excepcion
				if(persist) throw new InvalidRequestException("El archivo contiene un ejercicio que se solapa con uno o mas de los existentes, no se puede importar");
				
				// Si no se pidio persistir, se devuelve null para indicar que no se podra importar el ejercicio ni los asientos
				// (solo cuentas)
				return null;
			}
		}
		
		Ejercicio ej = new Ejercicio(org, inicio, finalizacion);
		return persist ? ejDao.save(ej) : ej;
	}
	
	@Override
	@Transactional
	public void run(ImportTask task, CuentaImportStrategy cuentaStrategy, boolean includeAsientos) {
		// Si la tarea no esta en PENDING, no se puede ejecutar
		if(task.getStatus() != ImportStatus.PENDING) throw new InvalidRequestException("La importacion esta en curso o ya se realizo");
		
		try {
			ImportTaskSummary summary = task.getSummary();
			
			// Si pidio importar asientos pero no se puede, se aborta
			if(includeAsientos && !task.puedeImportarEjercicio()) {
				String msg = String.format("La organizacion %s ya tiene ejercicio/s en las fechas (%s - %s), no se pueden importar ni el ejercicio ni los asientos", task.getOrganizacion().getNombre(), task.getEjercicio().getInicio(), task.getEjercicio().getFinalizacion());
				throw new InvalidRequestException(msg);
			}
			
			// Se cambia el status
			task.setStatus(ImportStatus.IMPORTING);
			
			if(task.getOrganizacion().getId() == null) summary.addEjercicio();
			
			// Se crea (y persiste) la organizacion, o se obtiene si ya existia
			Organizacion org = this.getOrCreate(task.getOrganizacion());
			
			List<ImportedCuenta> importedCuentas;
			List<ImportedAsiento> importedAsientos = null;
			List<ImportedMovimiento> importedMovimientos = null;
			
			// Se leen las cuentas, asientos y movimientos
			try(AccessReader reader = this.getReader(task.getFile())) {
				importedCuentas = reader.getCuentas();
				if(includeAsientos) {
					importedAsientos = reader.getAsientos();
					importedMovimientos = reader.getMovimientos();
				}
			}
			
			if(importedCuentas == null || importedCuentas.isEmpty()) {
				// Si no hay cuentas, solo se importara la organizacion
				// No puede haber imputaciones sin cuentas
				task.success();
				return;
			}
			
			// Se obtienen las monedas
			List<Moneda> monedas = monedaDao.findAll(); 
			if(monedas.isEmpty()) throw new ServerException("No se cargaron monedas");
			
			// Cuentas
			
			// Se crean las instancias de categorias o cuentas segun la estrategia
			List<CuentaBase> cuentas = CuentaConverter.getConverter(cuentaStrategy).convert(org, importedCuentas, monedas);
			
			// Mapa que asocia el codigo legacy con la cuenta
			Map<String, CuentaBase> codigoToCuenta = new HashMap<>();
			
			// Se itera cada categoria y cuenta, se la asocia a su padre (persistido) y se la persiste 
			for(int i = 0; i < cuentas.size(); i++) {
				CuentaBase item = cuentas.get(i);
				try {
					// Si tiene categoria padre pero no es la persistida, se referencia a la persistida
					// Hasta aqui esta asociada a otra instancia con el codigo
					if(item.getCategoria() != null && item.getCategoria().getId() == null) {
						
						// Se busca por codigo legacy
						CuentaBase cat = codigoToCuenta.get(item.getCategoria().getLegacyCodigo());
						
						// Si no se encuentra o no es categoria, se aborta
						if(cat == null) throw new InvalidRequestException("Tiene como categoria a " + item.getCategoria().getLegacyCodigo() + " pero este no existe");
						if(!(cat instanceof Categoria)) throw new InvalidRequestException("Tiene como categoria a '" + cat.getLegacyCodigo() + " - " + cat.getDescripcion() + "' pero esta es una cuenta imputable");
						
						// Se asocia
						item.setCategoria((Categoria)cat);
					}
					
					// Se busca o crea la cuenta o categoria
					if(item instanceof Categoria) {
						summary.addCategoria();
						codigoToCuenta.put(item.getLegacyCodigo(), this.getOrCreateCategoria(org, (Categoria) item, summary)); 
					} else {
						summary.addCuenta();
						codigoToCuenta.put(item.getLegacyCodigo(), this.getOrCreateCuenta(org, (Cuenta) item, summary));
					}
					
				} catch(Exception e) {
					throw new InvalidRequestException("Error al importar la categoria/cuenta " + item.getCodigo() + " - " + item.getDescripcion(), e);
				}
			}
			
			// Asientos
			if(includeAsientos) {
				// Se crea (y persiste) el ejercicio o se obtiene el existente
				if(task.getEjercicio().getId() == null) summary.addEjercicio();
				Ejercicio ejercicio = this.validarYCrearEjercicio(org, task.getEjercicio().getInicio(), task.getEjercicio().getFinalizacion(), true);
				
				// Se crean las instancias de asiento con sus imputaciones
				List<Asiento> asientos = this.convertAsientos(ejercicio, codigoToCuenta, importedAsientos, importedMovimientos);
				
				// Se persiste
				for(Asiento asiento : asientos) {
					try {
						asientoService.crear(asiento, ejercicio);
						summary.addAsiento();
						summary.addImputaciones(asiento.getImputaciones().size());
					} catch(Exception e) {
						throw new InvalidRequestException("Error al importar asiento " + asiento.getNumero() + " del " + asiento.getFecha(), e);
					}
				}
			}
			
			task.success();
		
		} catch(WebContabException e) {
			
			String message = e.getMessage();
			
			// Si la causa es un error de validacion, incluirlo en el mensaje a mostrar
			if(e.getCause() != null && e.getCause() instanceof InvalidRequestException || e.getCause() instanceof ConstraintViolationException) {
				message += ": " + e.getCause().getMessage();
			}
			task.error(message);
			throw e;
		} catch(Exception e) {
			task.error(e.getMessage());
			throw new ServerException("Error al importar archivo con uuid " + task.getUuid(), e);
		}
	}
	
	private AccessReader getReader(Path file) throws SQLException, IOException {
		AccessReader importer = new AccessReader();
		importer.open(file);
		return importer;
	}
	
	/**
	 * Se obtiene de la base la organizacion, y si es nueva, se crea.
	 * @param org 
	 * @return la organizacion persistida
	 */
	private Organizacion getOrCreate(Organizacion org) {
		Long id = org.getId();
		if(org.getId() != null) {
			// Si tiene ID (no es nueva, se busca)
			return orgDao.findById(id).orElseThrow(() -> new EntityNotFoundException(Organizacion.class, id));
		} else {
			// Es nueva, se crea (se usa este metodo para volver a chequear que siga siendo valida
			return this.validarYCrearOrg(org.getCuit(), org.getNombre(), true);
		}
	}
	
	/** 
	 * Obtiene una cuenta en base a su codigo legacy y, si no existe, la crea y la devuelve.
	 * @param org organizacion
	 * @param cuenta datos de la cuenta
	 * @param summary modelo con las estadisticas (para sumar una cuenta si se crea)
	 * @return cuenta persistida
	 */
	private Cuenta getOrCreateCuenta(Organizacion org, Cuenta cuenta, final ImportTaskSummary summary) {
		CuentaBase c = cuentaDao.findByOrganizacionAndLegacyCodigo(org, cuenta.getLegacyCodigo())
				.orElseGet(() -> {
					// Si no existe, se persiste y se incluye en la estadistica
					summary.addCuentaImportada();
					return cuentaService.crear(org, cuenta, cuenta.getNumero());
				});
	
		if(c instanceof Cuenta) return (Cuenta)c;
		// Si la entidad es una categoria, se aborta 
		throw new InvalidRequestException("El codigo " + cuenta.getLegacyCodigo() + " es una cuenta en el archivo importadao pero es una categoria en la base");
	}
	
	/** 
	 * Obtiene una categoria en base a su codigo legacy y, si no existe, la crea y la devuelve.
	 * @param org organizacion
	 * @param cat datos de la categoria
	 * @param summary modelo con las estadisticas (para sumar una categoria si se crea)
	 * @return categoria persistida
	 */
	private Categoria getOrCreateCategoria(Organizacion org,Categoria cat, final ImportTaskSummary summary) {
		CuentaBase c = cuentaDao.findByOrganizacionAndLegacyCodigo(org, cat.getLegacyCodigo())
				.orElseGet(() -> {
					// Si no existe, se persiste y se incluye en la estadistica
					summary.addCategoriaImportada();
					return categoriaService.crear(org, cat, cat.getNumero());
				});
	
		if(c instanceof Categoria) return (Categoria)c;
		
		// Si la entidad es una cuenta imputable, se aborta
		throw new InvalidRequestException("El codigo " + cat.getLegacyCodigo() + " es una categoria en el archivo importado pero es una cuenta en la base");
	}
	
	/**
	 * Crea la lista de asientos con sus imputaciones en base al contenido de las tablas asiento y movimiento.
	 * 
	 * @param ejercicio ejercicio de los asientos
	 * @param cuentas mapa de cuentas para asociar a las imputaciones
	 * @param asientos lista de asientos del MDB
	 * @param movimientos lista de movimientos del MDB
	 * @return
	 */
	private List<Asiento> convertAsientos(Ejercicio ejercicio, Map<String, CuentaBase> cuentas, List<ImportedAsiento> asientos, List<ImportedMovimiento> movimientos) {

		List<Asiento> result = new ArrayList<>();
		
		// Por cada asiento
		for(ImportedAsiento item : asientos) {
			Asiento asiento = new Asiento(ejercicio, item.numero, item.fecha, item.detalle, null);
			result.add(asiento);
			try {
			// Crea el asiento
				
				// Recorre la lsita de movimientos, buscando los que son del asiento actual
				for(ImportedMovimiento mov : movimientos) {
					if(mov.numAsiento == item.numero) {
						// Se obtiene la cuenta por codigo. Se lanza error si no existe o es una categoria
						CuentaBase cuenta = cuentas.get(mov.codigoCuenta);
						if(cuenta == null) throw new InvalidRequestException("Una imputacion hace referencia a la cuenta " + mov.codigoCuenta + " pero esta no existe");
						if(!(cuenta instanceof Cuenta)) throw new InvalidRequestException("Una imputacion hace referencia a la categoria " + mov.codigoCuenta + " pero solo se pueden imputar cuentas");
						
						asiento.agregarImputacion(new Imputacion((Cuenta)cuenta, mov.monto, mov.detalle));
					}
				}
				
			} catch(Exception e) {
				throw new InvalidRequestException("Error al leer asiento: " + asiento.getNumero() + " del " + asiento.getFecha(), e);
			}
		}
		
		return result;
	}
	
	/**
	 * Convierte las cuentas de WinContab en Cuentas de este sistema
	 *
	 */
	private static interface CuentaConverter {
		
		/**
		 * Convierte las categorias y cuentas importadas del MDB al formato actual
		 * @param org organizacion a las que se asociaran las cuentas
		 * @param cuentas lista de categorias y cuentas importadas
		 * @param monedas lista de monedas, se asocian las cuentas a la moneda default
		 * @return
		 */
		List<CuentaBase> convert(Organizacion org, List<ImportedCuenta> cuentas, List<Moneda> monedas);
		
		static CuentaConverter getConverter(CuentaImportStrategy strategy) {
			return strategy == CuentaImportStrategy.CODIGO ? new CuentaConverterByLegacyCodigo() : new CuentaConverterByNivel();
		}
	}
	
	/**
	 * Convertidor de Cuentas en base a su nivel.
	 * <p>
	 * Se ignora el codigo original, simplemente se ordenan las cuentas y se toma en cuenta su nivel.
	 * <br>
	 * Cada cuenta cuyo nivel sea mas alto que el anterior, se considera hijo de esta (aunque se salteen niveles).
	 * Al encontrar una cuenta de nivel mas bajo, se considera como una nueva jerarquia.
	 * </p>
	 * <p>
	 * Se asignaran nuevos codigos de cuenta en base al orden.
	 * </p>
	 * 
	 */
	private static class CuentaConverterByNivel implements CuentaConverter {
		
		/**
		 * Info del nivel (ultima cuenta y ultimo numero de cuenta hija)
		 */
		@AllArgsConstructor
		private static class Nivel {
			public CuentaBase cuenta;
			public Short lastNumber;
		}

		@Override
		public List<CuentaBase> convert(Organizacion org, List<ImportedCuenta> cuentas, List<Moneda> monedas) {
			// Se obtiene la moneda default
			Moneda defaultMoneda = monedas.stream().filter(m -> m.isDefault()).findFirst().orElse(monedas.get(0));
			
			List<CuentaBase> result = new ArrayList<>();
			
			// Mapa que contiene por cada nivel, la ultima cuenta asociada.
			Map<Integer, Nivel> niveles = new HashMap<>();
			// En el nivel 0 se pone un entrada raiz
			niveles.put(0, new Nivel(null, (short)0));
			
			// Se iteran las cuentas
			for(ImportedCuenta c : cuentas) {
				try {
					Nivel nivelPadre = getParentByNivel(c, niveles);
					Categoria parent = null;
					Short numero = null;
					
					// Si tiene padre, se lo asigna y se busca el proximo nuemro de cuenta
					if(nivelPadre != null) {
						parent = (Categoria)nivelPadre.cuenta;
						nivelPadre.lastNumber++;
						numero = nivelPadre.lastNumber;
					}
					
					CuentaBase item;
					if(!c.categoria) {
						item = new Cuenta(org, numero, c.descripcion, parent, false, c.ajustable, defaultMoneda);
						
						// Se pone el codigo legacy como alias (quitando los ceros al final)
						if(c.codigo != null) item.setAlias(c.codigo.replaceAll("0+$", ""));
					} else {
						item = new Categoria(org, numero, c.descripcion, parent, false);
					}
					item.setLegacyCodigo(c.codigo);
					result.add(item);
					
					// Se asigna esta categoria o cuenta como la nueva de este nivel y empezando de 0 la numeracion
					niveles.put(c.nivel, new Nivel(result.get(result.size() - 1), (short)0));
					
				} catch(Exception e) {
					throw new InvalidRequestException("Error al leer la cuenta " + c.codigo + " - " + c.descripcion, e);
				}
			}
			
			return result;
		}
		
		/**
		 * Obtiene la categoria padre de la actual
		 */
		private Nivel getParentByNivel(ImportedCuenta data, Map<Integer, Nivel> niveles) {
			// Se quitan del mapa las cuentas de niveles superiores o iguales al actual, ya que se termino su procesamiento.
			// Ej: Si esta es nivel 2, las cuentas de nivel 2 para arriba ya no estaran asociadas a las nuevas, en cambio la de nivel 1 si (es el padre)
			niveles.keySet().stream()
			.filter(nivel -> nivel >= data.nivel)
			.collect(Collectors.toSet())
			.forEach(niveles::remove);
			
			// Se busca el padre de esta cuenta. 
			// Se iteran los niveles desde el mas alto hacia abajo (si esta en nivel 4, desde el 3 al 0)
			// Se busca en todos los niveles porque puede haber "huecos" (pasa de nivel 2 a 4 sin pasar por el 3, entonces el nivel 4 sera hija de la de nivel 2)
			for(int i = data.nivel - 1; i >= 0; i--) {
				Nivel parent = niveles.get(i);
				if(parent != null) {
					if(parent.cuenta == null || parent.cuenta instanceof Categoria) return parent;
					throw new InvalidRequestException("Tiene como padre a '" + parent.cuenta.getDescripcion() + "' pero esta es una cuenta y no una categoria");
				}
			}
			// Si no se encontro padre, esta cuenta es raiz
			return null;
		}
	}
	
	/**
	 * Convertidor de Cuentas en base a su codigo original.
	 * <p>
	 * El codigo original es un String que se compone de grupos de digitos. Cada grupo es el numero en cada nivel.
	 * Tiene longitud fija, limitando a 6 niveles como maximo.  
	 * <br>
	 * Esta estrategia arma la jerarquia usando esos niveles y asigna las mismas numeraciones.
	 * </p>
	 * <p>
	 * Es necesario que los codigos esten correctamente organizados para que funcione.
	 * </p>
	 * 
	 */
	private static class CuentaConverterByLegacyCodigo implements CuentaConverter {

		@Override
		public List<CuentaBase> convert(Organizacion org, List<ImportedCuenta> cuentas, List<Moneda> monedas) {
			// Busca la moneda default
			Moneda defaultMoneda = monedas.stream().filter(m -> m.isDefault()).findFirst().orElse(monedas.get(0));
			
			List<CuentaBase> result = new ArrayList<>();
			
			for(ImportedCuenta c : cuentas) {
				try {
					
					// Obtiene el numero en cada nivel (ej: [1, 3, 2, 25])
					List<Short> path = getPath(c);
					
					// Obtiene el numero del ultimo nivel (ej: 25)
					Short numero = path.get(path.size() - 1);
					path.remove(path.size() - 1);
					
					// Busca el padre en base al codigo (todos los niveles menos el ultimo, ej: [1, 3, 2])
					Categoria parent = getParentByCodigo(result, getLegacyCodeFromPath(path), c);
					
					
					CuentaBase item;
					if(!c.categoria) {
						item = new Cuenta(org, numero, c.descripcion, parent, false, c.ajustable, defaultMoneda);
						
						// Se pone el codigo legacy como alias (quitando los ceros al final)
						if(c.codigo != null) item.setAlias(c.codigo.replaceAll("0+$", ""));
					} else {
						item = new Categoria(org, numero, c.descripcion, parent, false);
					}
					
					item.setLegacyCodigo(c.codigo);
					result.add(item);
				
				} catch(Exception e) {
					throw new InvalidRequestException("Error al leer la cuenta " + c.codigo + " - " + c.descripcion, e);
				}
			}
			
			return result;
		}
		
		/**
		 * Dada una cuenta, usa su codigo para obtener la lista de numeros de cada nivel.
		 */
		private List<Short> getPath(ImportedCuenta c) {
			List<Short> path = new ArrayList<>();
			// El 1 nivel es 1 digito
			short n = Short.parseShort(c.codigo.substring(0,1));
			path.add(n);
			
			// El 2 nivel es 1 digito
			n = Short.parseShort(c.codigo.substring(1,2));
			if(n > 0) path.add(n);
			
			// El 3 nivel es 1 digito
			n = Short.parseShort(c.codigo.substring(2,3));
			if(n > 0) path.add(n);
			
			// El 4 nivel son 2 digitos
			n = Short.parseShort(c.codigo.substring(3,5));
			if(n > 0) path.add(n);
			
			// El 5 nivel son 4 digitos
			n = Short.parseShort(c.codigo.substring(5,9));
			if(n > 0) path.add(n);
			
			// El 6 nivel son 3 digitos (o lo que quede)
			n = Short.parseShort(c.codigo.substring(9));
			if(n > 0) path.add(n);
			
			return path;
		}
		
		/**
		 * Arma un codigo original en base a la lista de numeros de cada nivel
		 * @param path
		 * @return
		 */
		private String getLegacyCodeFromPath(List<? extends Number> path) {
			// El primer nivel es 1 digito
			// El segundo es 1 digito
			// El tercero es 1 digito
			// El cuarto son 2 digitos
			// El quinto son 4 digitos
			// El sexto son 3 digitos
			if(path.isEmpty()) return null;
			String result = path.get(0).toString();
			result += (path.size() > 1) ? path.get(1).toString() : "0";
			result += (path.size() > 2) ? path.get(2).toString() : "0";
			
			result += (path.size() > 3 ? String.format("%02d", path.get(3)) : "00");
			result += (path.size() > 4 ? String.format("%04d", path.get(4)) : "0000");
			result += (path.size() > 5 ? String.format("%03d", path.get(5)) : "000");
			
			return result;
		}
		
		/**
		 * Busca una cuenta por codigo original.
		 * @param cuentas lista de cuentas procesadas hasta el momento
		 * @param codigo codigo a buscar
		 * @param data cuenta actual
		 * @return
		 */
		private Categoria getParentByCodigo(List<CuentaBase> cuentas, String codigo, ImportedCuenta data) {
			if(codigo == null) return null;
			CuentaBase c = cuentas.stream()
					.filter(cuenta -> cuenta.getLegacyCodigo().equals(codigo))
					.findFirst()
					.orElseThrow(() -> new InvalidRequestException("No se encontro la categoria padre con codigo : " + codigo));
		
			if(c instanceof Categoria) return (Categoria)c;
			throw new InvalidRequestException("Tiene como padre a '" + codigo + "' pero esta es una cuenta y no una categoria");
		}
	}
}
