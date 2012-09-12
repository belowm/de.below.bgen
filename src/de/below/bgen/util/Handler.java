package de.below.bgen.util;

/**
 * A generic callback method with one argument.
 * 
 * @author martin
 * 
 */
public interface Handler<T> {
	public void handle(T item);
}
