package com.mmoscovich.webcontab.dto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import lombok.Data;

/**
 * DTO de una pagina, con o sin total.
 *
 * @param <T> tipo de item de la pagina
 */
@Data
public class PageDTO<T> {
	private List<T> items;
	private int number;
	private int size;
	private boolean hasNext;
	private Long total;
	
	public static <T> PageDTO<T> adapt(Slice<T> page) {
		 return adapt(page, page.getContent());
	}
	
	public static <M, D> PageDTO<D> adapt(Slice<M> page, List<D> items) {
		PageDTO<D> p = new PageDTO<>();
		p.items = items;
		p.number = page.getNumber() + 1;
		p.size = page.getNumberOfElements();
		p.hasNext = page.hasNext();
		if(page instanceof Page) p.total = ((Page<M>) page).getTotalElements();
		return p;
	}
}
