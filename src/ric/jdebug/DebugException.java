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
 * Una DebugException indica que hay algun bug en el c�digo. Cuando se produce
 * una excepci�n de este tipo siempre habr� que hacer una cosa: corregir alg�n
 * punto del programa.
 *
 * Las dos clases derivadas de esta indican adem�s el responsable de realizar la
 * correcci�n: el invocante (PreconditionException) o el invocado (CheckException).
 *
 */

public abstract class DebugException extends RuntimeException {
  public DebugException(String msg) {
    super(msg);
  }
}
