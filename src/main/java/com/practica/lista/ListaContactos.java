package com.practica.lista;

import com.practica.genericas.Coordenada;
import com.practica.genericas.FechaHora;
import com.practica.genericas.PosicionPersona;

public class ListaContactos {
	private NodoTemporal lista;
	private int size;
	
	private class ResultadoBusqueda {
		NodoTemporal aux;
		NodoTemporal ant;
		boolean encontrado;

		ResultadoBusqueda(NodoTemporal aux, NodoTemporal ant, boolean encontrado) {
			this.aux = aux;
			this.ant = ant;
			this.encontrado = encontrado;
		}
	}

	private ResultadoBusqueda buscarNodoPorFecha(PosicionPersona p) {
		NodoTemporal aux = lista, ant = null;
		boolean salir = false;
		boolean encontrado = false;
		while (aux != null && !salir) {
			if(aux.getFecha().compareTo(p.getFechaPosicion()) == 0) {
				encontrado = true;
				salir = true;
			} else if(aux.getFecha().compareTo(p.getFechaPosicion()) < 0) {
				ant = aux;
				aux = aux.getSiguiente();
			} else if(aux.getFecha().compareTo(p.getFechaPosicion()) > 0) {
				salir = true;
			}
		}
		return new ResultadoBusqueda(aux, ant, encontrado);
	}

	private void insertarOActualizarCoordenada(NodoTemporal nodo, PosicionPersona p) {
		NodoPosicion npActual = nodo.getListaCoordenadas();
		NodoPosicion npAnt = null;
		boolean npEncontrado = false;
		while (npActual != null && !npEncontrado) {
			if(npActual.getCoordenada().equals(p.getCoordenada())) {
				npEncontrado = true;
				npActual.setNumPersonas(npActual.getNumPersonas() + 1);
			} else {
				npAnt = npActual;
				npActual = npActual.getSiguiente();
			}
		}
		if(!npEncontrado) {
			NodoPosicion npNuevo = new NodoPosicion(p.getCoordenada(), 1, null);
			if(nodo.getListaCoordenadas() == null)
				nodo.setListaCoordenadas(npNuevo);
			else
				npAnt.setSiguiente(npNuevo);
		}
	}

	private void crearYInsertarNuevoNodoTemporal(NodoTemporal ant, NodoTemporal aux, PosicionPersona p) {
		NodoTemporal nuevo = new NodoTemporal();
		nuevo.setFecha(p.getFechaPosicion());
		insertarOActualizarCoordenada(nuevo, p);
		if(ant != null) {
			nuevo.setSiguiente(aux);
			ant.setSiguiente(nuevo);
		} else {
			nuevo.setSiguiente(lista);
			lista = nuevo;
		}
		this.size++;
	}

	/**
	 * Insertamos en la lista de nodos temporales, y a la vez inserto en la lista de nodos de coordenadas.
	 * En la lista de coordenadas metemos el documento de la persona que está en esa coordenada
	 * en un instante
	 */
	public void insertarNodoTemporal (PosicionPersona p) {
		ResultadoBusqueda resultado = buscarNodoPorFecha(p);
		if(resultado.encontrado) {
			insertarOActualizarCoordenada(resultado.aux, p);
		} else {
			crearYInsertarNuevoNodoTemporal(resultado.ant, resultado.aux, p);
		}
	}
	
	private boolean buscarPersona (String documento, NodoPersonas nodo) {
		NodoPersonas aux = nodo;
		while(aux!=null) {
			if(aux.getDocumento().equals(documento)) {
				return true;				
			}else {
				aux = aux.getSiguiente();
			}
		}
		return false;
	}
	
	private void insertarPersona (String documento, NodoPersonas nodo) {
		NodoPersonas aux = nodo, nuevo = new NodoPersonas(documento, null);
		while(aux.getSiguiente()!=null) {				
			aux = aux.getSiguiente();				
		}
		aux.setSiguiente(nuevo);		
	}
	
	public int personasEnCoordenadas () {
		NodoPosicion aux = this.lista.getListaCoordenadas();
		if(aux==null)
			return 0;
		else {
			int cont;
			for(cont=0;aux!=null;) {
				cont += aux.getNumPersonas();
				aux=aux.getSiguiente();
			}
			return cont;
		}
	}
	
	public int tamanioLista () {
		return this.size;
	}

	public String getPrimerNodo() {
		NodoTemporal aux = lista;
		String cadena = aux.getFecha().getFecha().toString();
		cadena+= ";" +  aux.getFecha().getHora().toString();
		return cadena;
	}

	/**
	 * Métodos para comprobar que insertamos de manera correcta en las listas de 
	 * coordenadas, no tienen una utilidad en sí misma, más allá de comprobar que
	 * nuestra lista funciona de manera correcta.
	 */
	public int numPersonasEntreDosInstantes(FechaHora inicio, FechaHora fin) {
		if(this.size==0)
			return 0;
		NodoTemporal aux = lista;
		int cont = 0;
		int a;
		cont = 0;
		while(aux!=null) {
			if(aux.getFecha().compareTo(inicio)>=0 && aux.getFecha().compareTo(fin)<=0) {
				NodoPosicion nodo = aux.getListaCoordenadas();
				while(nodo!=null) {
					cont = cont + nodo.getNumPersonas();
					nodo = nodo.getSiguiente();
				}				
				aux = aux.getSiguiente();
			}else {
				aux=aux.getSiguiente();
			}
		}
		return cont;
	}
	
	
	
	public int numNodosCoordenadaEntreDosInstantes(FechaHora inicio, FechaHora fin) {
		if(this.size==0)
			return 0;
		NodoTemporal aux = lista;
		int cont = 0;
		int a;
		cont = 0;
		while(aux!=null) {
			if(aux.getFecha().compareTo(inicio)>=0 && aux.getFecha().compareTo(fin)<=0) {
				NodoPosicion nodo = aux.getListaCoordenadas();
				while(nodo!=null) {
					cont = cont + 1;
					nodo = nodo.getSiguiente();
				}				
				aux = aux.getSiguiente();
			}else {
				aux=aux.getSiguiente();
			}
		}
		return cont;
	}
	
	
	
	@Override
	public String toString() {
		String cadena="";
		int a,cont;
		cont=0;
		NodoTemporal aux = lista;
		for(cont=1; cont<size; cont++) {
			cadena += aux.getFecha().getFecha().toString();
			cadena += ";" +  aux.getFecha().getHora().toString() + " ";
			aux=aux.getSiguiente();
		}
		cadena += aux.getFecha().getFecha().toString();
		cadena += ";" +  aux.getFecha().getHora().toString();
		return cadena;
	}
	
	
	
}
