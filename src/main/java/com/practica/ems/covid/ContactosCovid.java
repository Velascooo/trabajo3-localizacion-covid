package com.practica.ems.covid;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.practica.excecption.EmsDuplicateLocationException;
import com.practica.excecption.EmsDuplicatePersonException;
import com.practica.excecption.EmsInvalidNumberOfDataException;
import com.practica.excecption.EmsInvalidTypeException;
import com.practica.excecption.EmsLocalizationNotFoundException;
import com.practica.excecption.EmsPersonNotFoundException;
import com.practica.genericas.Constantes;
import com.practica.genericas.Coordenada;
import com.practica.genericas.FechaHora;
import com.practica.genericas.Persona;
import com.practica.genericas.PosicionPersona;
import com.practica.lista.ListaContactos;


public class ContactosCovid {
	private Poblacion poblacion;
	private Localizacion localizacion;
	private ListaContactos listaContactos;
	private final String tipoPersona = "PERSONA";
	private final String tipoLocalizacion = "LOCALIZACION";

	public ContactosCovid() {
		this.poblacion = new Poblacion();
		this.localizacion = new Localizacion();
		this.listaContactos = new ListaContactos();
	}

	public Poblacion getPoblacion() {
		return poblacion;
	}

	public void setPoblacion(Poblacion poblacion) {
		this.poblacion = poblacion;
	}

	public Localizacion getLocalizacion() {
		return localizacion;
	}

	public void setLocalizacion(Localizacion localizacion) {
		this.localizacion = localizacion;
	}



	public ListaContactos getListaContactos() {
		return listaContactos;
	}

	public void setListaContactos(ListaContactos listaContactos) {
		this.listaContactos = listaContactos;
	}

	public void loadData(String data, boolean reset) throws EmsInvalidTypeException, EmsInvalidNumberOfDataException,
			EmsDuplicatePersonException, EmsDuplicateLocationException {
		// borro información anterior
		if (reset) {
			this.poblacion = new Poblacion();
			this.localizacion = new Localizacion();
			this.listaContactos = new ListaContactos();
		}
		String[] lineas = dividirEntrada(data);
		for (String linea : lineas) {
			String[] datos = this.dividirLineaData(linea);
			if (!datos[0].equals(tipoPersona) && !datos[0].equals(tipoLocalizacion)) {
				throw new EmsInvalidTypeException();
			}
			if (datos[0].equals(tipoPersona)) {
				if (datos.length != Constantes.MAX_DATOS_PERSONA) {
					throw new EmsInvalidNumberOfDataException("El número de datos para PERSONA es menor de 8");
				}
				this.poblacion.addPersona(this.crearPersona(datos));
			}
			if (datos[0].equals(tipoLocalizacion)) {
				if (datos.length != Constantes.MAX_DATOS_LOCALIZACION) {
					throw new EmsInvalidNumberOfDataException("El número de datos para LOCALIZACION es menor de 6");
				}
				PosicionPersona pp = this.crearPosicionPersona(datos);
				this.localizacion.addLocalizacion(pp);
				this.listaContactos.insertarNodoTemporal(pp);
			}
		}
	}

	public void loadDataFile(String fichero, boolean reset) {
		File archivo = null;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		String[] lineas = null;
		String linea = null;
		loadDataFile(fichero, reset, archivo, fileReader, bufferedReader, lineas, linea);

	}

	@SuppressWarnings("resource")
	public void loadDataFile(String fichero, boolean reset, File archivo, FileReader fileReader, BufferedReader bufferedReader, String[] lineas, String linea ) {
		try {
			// Apertura del fichero y creacion de BufferedReader para poder
			// hacer una lectura comoda (disponer del metodo readLine()).
			archivo = new File(fichero);
			fileReader = new FileReader(archivo);
			bufferedReader = new BufferedReader(fileReader);
			if (reset) {
				this.poblacion = new Poblacion();
				this.localizacion = new Localizacion();
				this.listaContactos = new ListaContactos();
			}
			/**
			 * Lectura del fichero línea a línea. Compruebo que cada línea
			 * tiene el tipo PERSONA o LOCALIZACION y cargo la línea de datos en la
			 * lista correspondiente. Sino viene ninguno de esos tipos lanzo una excepción
			 */
			while ((linea = bufferedReader.readLine()) != null) {
				lineas = dividirEntrada(linea.trim());
				for (String lineaInterna : lineas) {
					procesarLinea(lineaInterna);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// En el finally cerramos el fichero, para asegurarnos
			// que se cierra tanto si todo va bien como si salta
			// una excepcion.
			try {
				if (null != fileReader) {
					fileReader.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	public int findPersona(String documento) throws EmsPersonNotFoundException {
		try {
			return this.poblacion.findPersona(documento);
		} catch (EmsPersonNotFoundException e) {
			throw new EmsPersonNotFoundException();
		}
	}

	public int findLocalizacion(String documento, String fecha, String hora) throws EmsLocalizationNotFoundException {
		try {
			return localizacion.findLocalizacion(documento, fecha, hora);
		} catch (EmsLocalizationNotFoundException e) {
			throw new EmsLocalizationNotFoundException();
		}
	}

	public List<PosicionPersona> localizacionPersona(String documento) throws EmsPersonNotFoundException {
		int cont = 0;
		List<PosicionPersona> lista = new ArrayList<>();
		Iterator<PosicionPersona> it = this.localizacion.getLista().iterator();
		while (it.hasNext()) {
			PosicionPersona pp = it.next();
			if (pp.getDocumento().equals(documento)) {
				cont++;
				lista.add(pp);
			}
		}
		if (cont == 0)
			throw new EmsPersonNotFoundException();
		else
			return lista;
	}

	public boolean delPersona(String documento) throws EmsPersonNotFoundException {
		int cont = 0;
		int pos = -1;
		Iterator<Persona> it = this.poblacion.getLista().iterator();
		while (it.hasNext()) {
			Persona personaActual = it.next();
			if (personaActual.getDocumento().equals(documento)) {
				pos = cont;
			}
			cont++;
		}
		if (pos == -1) {
			throw new EmsPersonNotFoundException();
		}
		this.poblacion.getLista().remove(pos);
		return false;
	}

	private String[] dividirEntrada(String input) {
		return input.split("\\n");
	}

	private String[] dividirLineaData(String data) {
		return data.split("\\;");
	}

	private Persona crearPersona(String[] data) {
		Persona persona = new Persona();
		for (int i = 1; i < Constantes.MAX_DATOS_PERSONA; i++) {
			String s = data[i];
			switch (i) {
			case 1:
				persona.setDocumento(s);
				break;
			case 2:
				persona.setNombre(s);
				break;
			case 3:
				persona.setApellidos(s);
				break;
			case 4:
				persona.setEmail(s);
				break;
			case 5:
				persona.setDireccion(s);
				break;
			case 6:
				persona.setCp(s);
				break;
			case 7:
				persona.setFechaNacimiento(parsearFecha(s));
				break;
				default:
					break;
			}
		}
		return persona;
	}

	private PosicionPersona crearPosicionPersona(String[] data) {
		PosicionPersona posicionPersona = new PosicionPersona();
		String fecha = null;
		String hora;
		float latitud = 0;
		float longitud;
		for (int i = 1; i < Constantes.MAX_DATOS_LOCALIZACION; i++) {
			String s = data[i];
			switch (i) {
			case 1:
				posicionPersona.setDocumento(s);
				break;
			case 2:
				fecha = data[i];
				break;
			case 3:
				hora = data[i];
				posicionPersona.setFechaPosicion(parsearFecha(fecha, hora));
				break;
			case 4:
				latitud = Float.parseFloat(s);
				break;
			case 5:
				longitud = Float.parseFloat(s);
				posicionPersona.setCoordenada(new Coordenada(latitud, longitud));
				break;
				default:
					break;
			}
		}
		return posicionPersona;
	}

	private FechaHora parsearFecha (String fecha) {
		int dia;
		int mes;
		int anio;
		String[] valores = fecha.split("\\/");
		dia = Integer.parseInt(valores[0]);
		mes = Integer.parseInt(valores[1]);
		anio = Integer.parseInt(valores[2]);
		FechaHora fechaHora = new FechaHora(dia, mes, anio, 0, 0);
		return fechaHora;
	}

	private FechaHora parsearFecha (String fecha, String hora) {
		int dia;
		int mes;
		int anio;
		String[] valores = fecha.split("\\/");
		dia = Integer.parseInt(valores[0]);
		mes = Integer.parseInt(valores[1]);
		anio = Integer.parseInt(valores[2]);
		int minuto;
		int segundo;
		valores = hora.split("\\:");
		minuto = Integer.parseInt(valores[0]);
		segundo = Integer.parseInt(valores[1]);
		FechaHora fechaHora = new FechaHora(dia, mes, anio, minuto, segundo);
		return fechaHora;
	}

	private void procesarLinea(String linea) throws EmsInvalidTypeException, EmsInvalidNumberOfDataException, EmsDuplicatePersonException, EmsDuplicateLocationException {
	    String[] datos = this.dividirLineaData(linea);
	    if (!datos[0].equals(tipoPersona) && !datos[0].equals(tipoLocalizacion)) {
	        throw new EmsInvalidTypeException();
	    }
	    if (datos[0].equals(tipoPersona)) {
	        if (datos.length != Constantes.MAX_DATOS_PERSONA) {
	            throw new EmsInvalidNumberOfDataException("El número de datos para PERSONA es menor de 8");
	        }
	        this.poblacion.addPersona(this.crearPersona(datos));
	    }
	    if (datos[0].equals(tipoLocalizacion)) {
	        if (datos.length != Constantes.MAX_DATOS_LOCALIZACION) {
	            throw new EmsInvalidNumberOfDataException("El número de datos para LOCALIZACION es menor de 6");
	        }
	        PosicionPersona pp = this.crearPosicionPersona(datos);
	        this.localizacion.addLocalizacion(pp);
	        this.listaContactos.insertarNodoTemporal(pp);
	    }
	}
}
