/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package samples.ejb.subclassing.servlet;

import java.io.*;
import java.util.*; 
import javax.servlet.*; 
import javax.naming.*; 
import javax.servlet.http.*; 

import samples.ejb.subclassing.ejb.*; 
import com.sun.jdbcra.spi.JdbcSetupAdmin;

public class SimpleBankServlet extends HttpServlet {  


  InitialContext initContext = null;
  CustomerSavingsHome customerSavingsHome = null;
  CustomerCheckingHome customerCheckingHome = null;
  CustomerSavings customerSavings = null;
  CustomerChecking customerChecking = null;
  Hashtable env = new java.util.Hashtable(1);
  String JNDIName = null;
  Object objref = null;

  public void init()
  {
  }

  public void doGet (HttpServletRequest request,HttpServletResponse response) 
        throws ServletException, IOException { 
    doPost(request, response);
  }  

  /** handles the HTTP POST operation **/ 
  public void doPost (HttpServletRequest request,HttpServletResponse response) 
        throws ServletException, IOException {


      try{
            InitialContext ic = new InitialContext();
          //ic.lookup("jms/ConnectionFactory");
      }catch(Exception e){
          e.printStackTrace();
      }
      
    doLookup();
    System.out.println("SimpleBankServlet is executing");
    String SSN = request.getParameter("SSN");

    String message = "";
    String jsp = "";
    String lastName = "";
    String firstName = "";
    String address1 = "";
    String address2 = "";
    String city = "";
    String state = "";
    String zipCode = "";
    long currentSavingsBalance = 0;
    long currentCheckingBalance = 0;
     
    String action = request.getParameter("action");
    if (action.equals("Create"))
    {
      message = "Add Customer";
      jsp = "/SimpleBankAdd.jsp";
    }
    else if (action.equals("Add Customer")) 
    {
      System.out.println("Add Customer button pressed");
      SSN = request.getParameter("SSN");
      lastName = request.getParameter("lastName");
      firstName = request.getParameter("firstName");
      address1 = request.getParameter("address1");
      address2 = request.getParameter("address2");
      city = request.getParameter("city");
      state = request.getParameter("state");
      zipCode = request.getParameter("zipCode");

      try {
        System.out.println("Creating the customer savings remote bean");
        customerSavings = customerSavingsHome.create(SSN, lastName, firstName, address1, address2, city, state, zipCode);
      } catch (Exception e) {
        System.out.println("Could not create the customer savings remote bean : " + e.toString());
	throw new ServletException(e.getMessage());
        //return;
      }
      message = "Customer Added.";
      jsp = "/SimpleBankMessage.jsp";
    }
    else if (action.equals("Edit"))
    {
      try {
        System.out.println("Finding the customer savings remote bean");
        customerSavings = customerSavingsHome.findByPrimaryKey(SSN);
      } catch (Exception e) {
        System.out.println("Could not find the customer remote bean : " + e.toString());
	throw new ServletException(e.getMessage());
        //return;
      }
      jsp = "/SimpleBankEdit.jsp";
    }
    else if (action.equals("Delete"))
    {
      try {
        System.out.println("Finding the customer savings remote bean");
        customerSavings = customerSavingsHome.findByPrimaryKey(SSN);
      } catch (Exception e) {
        System.out.println("Could not find the customer savings remote bean : " + e.toString());
	throw new ServletException(e.getMessage());
        //return;
      }
      message = "Delete Customer";
      jsp = "/SimpleBankDelete.jsp";
    }
    else if (action.equals("Delete Customer"))
    {
      try {
        customerSavingsHome.findByPrimaryKey(SSN).remove();
      } catch (Exception e) {
        System.out.println("Could not delete the customer savings bean : " + e.toString());
	throw new ServletException(e.getMessage());
        //return;
      }
      message = "Customer Deleted.";
      jsp = "/SimpleBankMessage.jsp";
    }  


    else if (action.equals("Update"))
    {
      try {
        System.out.println("Finding the customersavings remote bean");
        customerSavings = (CustomerSavings)customerSavingsHome.findByPrimaryKey(SSN);
      } catch (Exception e) {
        System.out.println("Could not find the customer savings remote bean : " + e.toString());
	throw new ServletException(e.getMessage());
        //return;
      }

      try {
        System.out.println("Finding the customerchecking remote bean");
        customerChecking = (CustomerChecking)customerCheckingHome.findByPrimaryKey(SSN);
      } catch (Exception e) {
        System.out.println("Could not find the customer checking remote bean : " + e.toString());
	throw new ServletException(e.getMessage());
        //return;
      }

      System.out.println("Transaction Complete");
      String operationSavings = request.getParameter("operationSavings");
      System.out.println("operationSavings = " + operationSavings);
      String operationChecking = request.getParameter("operationChecking");
      System.out.println("operationChecking = " + operationChecking);
      String amountSavings = request.getParameter("amountSavings");
      String amountChecking = request.getParameter("amountChecking");
      if (operationSavings.equals("credit"))
      {
        customerSavings.doCredit(Long.parseLong(amountSavings), CustomerEJB.SAVINGS);
        customerChecking.doCredit(Long.parseLong(amountSavings), CustomerEJB.SAVINGS);
      } else {
        customerSavings.doDebit(Long.parseLong(amountSavings), CustomerEJB.SAVINGS);
        customerChecking.doDebit(Long.parseLong(amountSavings), CustomerEJB.SAVINGS);
      }
      if (operationChecking.equals("credit"))
      {
        customerChecking.doCredit(Long.parseLong(amountChecking), CustomerEJB.CHECKING);
        customerSavings.doCredit(Long.parseLong(amountChecking), CustomerEJB.CHECKING);
      } else {
        customerChecking.doDebit(Long.parseLong(amountChecking), CustomerEJB.CHECKING);
        customerSavings.doDebit(Long.parseLong(amountChecking), CustomerEJB.CHECKING);
      }
      jsp = "/SimpleBankEdit.jsp";
    }

    if (customerSavings != null)
    {
      lastName = customerSavings.getLastName();
      firstName = customerSavings.getFirstName();
      address1 = customerSavings.getAddress1();
      address2 = customerSavings.getAddress2();
      city = customerSavings.getCity();
      state = customerSavings.getState();
      zipCode = customerSavings.getZipCode();
      SSN = customerSavings.getSSN();
      currentSavingsBalance = customerSavings.getSavingsBalance();
      currentCheckingBalance = customerSavings.getCheckingBalance();
    }

    System.out.println("storing the values in the request object");
    request.setAttribute("lastName", lastName);
    request.setAttribute("firstName", firstName);
    request.setAttribute("address1", address1);
    request.setAttribute("address2", address2);
    request.setAttribute("city", city);
    request.setAttribute("state", state);
    request.setAttribute("zipCode", zipCode);
    request.setAttribute("SSN", SSN);
    request.setAttribute("currentSavingsBalance", String.valueOf(currentSavingsBalance));
    request.setAttribute("currentCheckingBalance", String.valueOf(currentCheckingBalance));
    request.setAttribute("message", message);
    response.setContentType("text/html");
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(jsp);
    dispatcher.include(request, response);
    return;
  } 

  public void doLookup() 
  {
    try {
      initContext = new javax.naming.InitialContext();
    } catch (Exception e) {
      System.out.println("Exception occured when creating InitialContext: " + e.toString());
      return;
    }

    try {
      JdbcSetupAdmin ja = (JdbcSetupAdmin) initContext.lookup("eis/jdbcAdmin");
      if (ja.checkSetup() == false) {
         throw new RuntimeException("JDBC Setup Wroing");
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }

    try {
      System.out.println("Looking up customer savings bean home interface");
      JNDIName = "java:comp/env/ejb/customerSavings";
      System.out.println("Looking up: " + JNDIName);
      customerSavingsHome = (CustomerSavingsHome)initContext.lookup(JNDIName);
    } catch (Exception e) {
      System.out.println("Customer savings bean home not found - Is the bean registered with JNDI? : " + e.toString());
      return;
    }

    try {
      System.out.println("Looking up customer checking bean home interface");
      JNDIName = "java:comp/env/ejb/customerChecking";
      System.out.println("Looking up: " + JNDIName);
      customerCheckingHome = (CustomerCheckingHome)initContext.lookup(JNDIName);
    } catch (Exception e) {
      System.out.println("Customer checking bean home not found - Is the bean registered with JNDI? : " + e.toString());
      return;
    }
  }
}
