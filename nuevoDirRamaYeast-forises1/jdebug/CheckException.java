/*
 *  Yeast-Server for Java
 *
 *  Copyright (c) 2011, Francisco Jos� Garc�a Izquierdo. University of La
 *  Rioja. Mathematics and Computer Science Department. All Rights Reserved.
 *
 *  Contributing Author(s):
 *
 *     Francisco J. Garc�a Iquierdo <francisco.garcia@unirioja.es>
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
 * Una clase lanza una CheckException cuando detecta que est� en un estado
 * inconsistente. A dicho estado se ha llegado por un fallo en la codificaci�n
 * de la misma y no por un uso inadecuado (ser�a una PreconditionException).
 *
 * El que ha codificado el m�todo que produce la excepci�n tiene un bug que
 * corregir (no el que lo ha llamado).
 *
 * Es importante que los objetos controlen la consistencia de su estado para
 * verificar en todo momento su disponibilidad. En caso de detectar alguna
 * situaci�n an�mala deber� avisar al cliente para que �ste descubra el m�dulo
 * incorrecto en vez de que pierda el tiempo revisando su c�digo.
 *
 */

public class CheckException extends DebugException {
  public CheckException(String msg) {
    super(msg);
  }
}
