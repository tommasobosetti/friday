/*
 * WebProgramming Project - Shopping List 
 * 2017-2018
 * Tommaso Bosetti - Sebastiano Chiari - Leonardo Remondini - Marta Toniolli
 */
package it.unitn.aa1718.webprogramming.servlets;

import it.unitn.aa1718.webprogramming.connection.DAOFactory;
import it.unitn.aa1718.webprogramming.dao.MyCookieDAO;
import it.unitn.aa1718.webprogramming.dao.UserDAO;
import it.unitn.aa1718.webprogramming.dao.entities.MySQLMyCookieDAOImpl;
import it.unitn.aa1718.webprogramming.dao.entities.MySQLUserDAOImpl;
import it.unitn.aa1718.webprogramming.encrypt.DBSecurity;
import it.unitn.aa1718.webprogramming.extra.Library;
import it.unitn.aa1718.webprogramming.friday.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class securityServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods. 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet securityServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet securityServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * Metodo GET non implementato.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * Metodo POST della Servlet  cripta i dati in input della request e li processa. 
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        DAOFactory mySqlFactory = DAOFactory.getDAOFactory();
        UserDAO riverDAO = mySqlFactory.getUserDAO();
        List users = null;
        UserDAO userDAO = new MySQLUserDAOImpl();        
        Library library = new Library();
        DBSecurity encrypt = new DBSecurity();
        
        String typeChange = request.getParameter("typeChange");
        
        String emailSession = (String) (request.getSession()).getAttribute("emailSession");
        String dbpassword = userDAO.getPasswordByUserEmail(emailSession);
        // forse per recuperare tutti i dati qua sotto dell'utente loggate aveva più senso fare come per la password invece che far passare tutto tramite la sessione. 
        String name = (String) (request.getSession()).getAttribute("nameUserSession");
        String surname = (String) (request.getSession()).getAttribute("surnameUserSession");
        String avatar = (String) (request.getSession()).getAttribute("avatarUserSession");
        boolean admin = (boolean) (request.getSession()).getAttribute("adminUserSession");
        boolean list_owner = (boolean) (request.getSession()).getAttribute("list_OwnerUserSession");
        boolean confirmed = (boolean) (request.getSession()).getAttribute("confirmedUserSession");
        
        
         
        
        switch (typeChange) {
            case "password": library.changePassword(request, response, encrypt, library, userDAO, emailSession, name, surname, avatar, admin, list_owner, confirmed); break;
            case "email": library.changeEmail(request, response, encrypt, library, userDAO, dbpassword, name, surname, avatar, admin, list_owner, confirmed); break;
            case "personal": library.changePersonal(request, response, encrypt, library, userDAO, emailSession, dbpassword, name, surname, avatar, admin, list_owner, confirmed); break;
            case "admin": 
                
                String psw = (String) request.getParameter("passwordforAdmin");
                String encryptPsw = encrypt.setSecurePassword(psw, emailSession);
                
                
                if(encryptPsw.equals(dbpassword)){
                    library.changeAdmin(request, response, encrypt, library, userDAO, emailSession, dbpassword, name, surname, avatar, list_owner, confirmed); 
                    
                    
                    break;
                } else {
                    //Redirezionamento ad admin.jsp se la password è sbagliata. Altrimenti lasciamo error.jsp :)
                    response.sendRedirect("error.jsp");
                    break;
                }
                
                
                 case "deleteAccount":
                String deletingEmail = request.getParameter("deleteEmail");
                if (deletingEmail.equals(emailSession)){
                    String deletingPassword = request.getParameter("deletePassword");
                    
                    String passHash = encrypt.setSecurePassword(deletingPassword, deletingEmail);
                    
                    if (passHash.equals(dbpassword)) {
                        userDAO.deleteUser(new User(deletingEmail, request.getParameter("deletePassword"), name, surname, avatar, admin, list_owner, confirmed));
                       
                        
                        HttpSession session = request.getSession();
                        
                        
                        MyCookieDAO myCookieDAO = new MySQLMyCookieDAOImpl();
                        myCookieDAO.deleteCookieByCookieID((int)session.getAttribute("cookieIDSession"));
        
                     
                        session.setAttribute("emailSession", null);
                        session.setAttribute("cookieIDSession", null);
                        String boolEmailSession = request.getParameter("boolEmailSession");
                        if ("false".equals(boolEmailSession)){
                            request.setAttribute("boolEmailSession", false);
                        }
                        session.invalidate();
                        
                        response.sendRedirect("deleteUserSuccess.jsp");
                    } else {
                        response.sendRedirect("error.jsp");
                    }
                    
                } else {
                    response.sendRedirect("error.jsp");
                };
                break;
            default: response.sendRedirect("myaccount.jsp");
        }
        
    }
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
