/*
 *  Yeast-Server for Java
 *
 *  Copyright (c) 2011, Francisco José García Izquierdo. University of La
 *  Rioja. Mathematics and Computer Science Department. All Rights Reserved.
 *
 *  Contributing Author(s):
 *
 *     Francisco J. García Iquierdo <francisco.garcia@unirioja.es>
 *
 *  COPYRIGHT PERMISSION STATEMENT:
 *
 *  This file is part of Yeast-Server for Java.
 *
 *  Yeast-Server for Java is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 3 of the
 *  License, or any later version.
 *
 *  Yeast-Server for Java is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  This software uses and includes a modificaction of jTidy-SNAPSHOT 8.0
 *  (Copyright (c) 1998-2000 World Wide Web Consortium).
 *
 */
package ric.jdebug;

/**
 * Una clase lanza una PreconditionException cuando detecta que el cliente no la
 * está usando de la forma adecuada.
 *
 * El que llama al método que produce la excepción tiene un bug que corregir
 * (no el programador del método).
 *
 * Al implementar las clases se deberá intentar detectar todos los posibles
 * errores que pueda cometer el cliente. De esta manera se consiguen dos
 * importantes objetivos:
 * - Que no pueda corromper al objeto con ninguna secuencia de invocaciones.
 * - Informarle de sus errores para facilitarle la vida :)
 *
 * En esta clase es fundamental el mensaje del error (msg). De nada sirve que
 * se le indique al cliente que hay un error si no se le dice dónde está y como
 * corregirlo.
 *
 */

public class PreconditionException extends DebugException {
  public PreconditionException(String msg) {
    super(msg);
  }
}
