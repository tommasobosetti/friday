/*
 * WebProgramming Project - Shopping List 
 * 2017-2018
 * Tommaso Bosetti - Sebastiano Chiari - Leonardo Remondini - Marta Toniolli
 */
package it.unitn.aa1718.webprogramming.servlets;

import it.unitn.aa1718.webprogramming.connection.DAOFactory;
import it.unitn.aa1718.webprogramming.dao.MessageDAO;
import it.unitn.aa1718.webprogramming.dao.ProductDAO;
import it.unitn.aa1718.webprogramming.dao.ProductListDAO;
import it.unitn.aa1718.webprogramming.dao.SharingDAO;
import it.unitn.aa1718.webprogramming.dao.SharingProductDAO;
import it.unitn.aa1718.webprogramming.dao.ShoppingListDAO;
import it.unitn.aa1718.webprogramming.dao.UserDAO;
import it.unitn.aa1718.webprogramming.dao.entities.MySQLMessageDAOImpl;
import it.unitn.aa1718.webprogramming.dao.entities.MySQLProductDAOImpl;
import it.unitn.aa1718.webprogramming.dao.entities.MySQLProductListDAOImpl;
import it.unitn.aa1718.webprogramming.dao.entities.MySQLSharingDAOImpl;
import it.unitn.aa1718.webprogramming.dao.entities.MySQLSharingProductDAOImpl;
import it.unitn.aa1718.webprogramming.dao.entities.MySQLShoppingListDAOImpl;
import it.unitn.aa1718.webprogramming.dao.entities.MySQLUserDAOImpl;
import it.unitn.aa1718.webprogramming.extra.Library;
import it.unitn.aa1718.webprogramming.friday.Message;
import it.unitn.aa1718.webprogramming.friday.ProductList;
import it.unitn.aa1718.webprogramming.friday.Sharing;
import it.unitn.aa1718.webprogramming.friday.SharingProduct;
import it.unitn.aa1718.webprogramming.friday.ShoppingList;
import it.unitn.aa1718.webprogramming.friday.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class sharingListServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     * 
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
            out.println("<title>Servlet sharingListServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet sharingListServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * Metodo GET della Servlet che si occupa di condividere la lista con l'utente specificato. 
     * Gestisce l'eliminazione, la condivisione,l'aggiunta di elementi e le informazioni di una lista.
     * 
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        int azioneLista = Integer.parseInt(request.getParameter("azioneLista"));
        int listaScelta = 0;
        SharingDAO sharingDAO = new MySQLSharingDAOImpl();
        ShoppingListDAO shoppingListDAO = new MySQLShoppingListDAOImpl();
        ProductListDAO productListDAO = new MySQLProductListDAOImpl();
        SharingProductDAO sharingProductDAO = new MySQLSharingProductDAOImpl();
        ProductDAO productDAO = new MySQLProductDAOImpl();
        List productList = null;
        Library library = new Library();
        HttpSession session = request.getSession();
          
            session.setAttribute("passaggioServlet", true);
        
        switch (azioneLista) {
            case 2: 
                listaScelta = Integer.parseInt(request.getParameter("listToShare"));
                String email = request.getParameter("invitationEmail");
                boolean modify = false;
                boolean addRemProd = false;
                boolean listDelete = false;
                
                if(email.equals(session.getAttribute("emailSession"))){
                   response.sendRedirect("handlingListServlet");
                   break;
                } else {
                
                    if (request.getParameter("modify") != null && Integer.parseInt(request.getParameter("modify")) == 1){
                        modify = true;
                    }
                    if (request.getParameter("addRemProd") != null && Integer.parseInt(request.getParameter("addRemProd")) == 1){
                        addRemProd = true;
                    }
                    if (request.getParameter("listDelete") != null && Integer.parseInt(request.getParameter("listDelete")) == 1){
                        listDelete = true;
                    }
                    sharingDAO.createSharing(new Sharing(email, listaScelta, modify, addRemProd, listDelete));
                    productList = productListDAO.getPIDsByLID(listaScelta);
                    for (int i=0; i<productList.size(); i++){
                        if ((productDAO.getProduct((((ProductList)productList.get(i)).getPID()), (String)session.getAttribute("emailSession"))).getEmail().equals((String)session.getAttribute("emailSession"))){
                            sharingProductDAO.createSharingProduct(new SharingProduct(email, ((ProductList)productList.get(i)).getPID()));
                        }
                    }
                    response.sendRedirect("handlingListServlet?selectedList="+listaScelta);
                    break;
                }
            case 3:
                // questa parte va decisamente rivista, ora come ora dovrebbe essere la chat
         
                //inizializzo i DAO e sessione
                DAOFactory mySqlFactory = DAOFactory.getDAOFactory();
                //SharingDAO sharingDAO1 = new MySQLSharingDAOImpl();
                MessageDAO messageDAO = new MySQLMessageDAOImpl();
                UserDAO userDAO = new MySQLUserDAOImpl();
                int listaSelezionata = Integer.parseInt(request.getParameter("messageToList"));

                //aggiungo messaggi
                if(request.getParameter("newMessage") != null){
                    
                    if(request.getParameter("mewMessage").length() < 500){
                        Message newMessage = new Message(library.LastEntryTable("messageID", "messages"), (int)session.getAttribute("selectedList"), (String)session.getAttribute("emailSession"), request.getParameter("newMessage"));
                        messageDAO.createMessage(newMessage);
                    } else {
                        response.sendRedirect("error.jsp");
                    }
                       
                } 
                

                //ottengo valori
                int LID = listaSelezionata;
                List partecipanti = sharingDAO.getAllEmailsbyList(LID);
                List messaggi = messageDAO.getMessagesByLID(LID);

                //salvo i partecipanti in modo da poterli passare alla jsp
                String[][] PartecipantiResult = new String[partecipanti.size()][3];

                for(int i=0; i<partecipanti.size(); i++){
                    User tmp = userDAO.getUser(((Sharing)partecipanti.get(i)).getEmail());

                    PartecipantiResult[i][0] = tmp.getAvatar();
                    PartecipantiResult[i][1] = tmp.getName();
                    PartecipantiResult[i][2] = tmp.getSurname();
                    //System.out.println(PartecipantiResult[i][0]+" "+PartecipantiResult[i][1]+" "+PartecipantiResult[i][2]);
                }

                //salvo i messaggi in modo da poterli passare alla jsp
                String[][] MessaggiResult = new String[messaggi.size()][5];

                for(int i=0; i<messaggi.size(); i++){
                    Message tmp = (Message)messaggi.get(i);

                    MessaggiResult[i][0] = (userDAO.getUser(tmp.getSender())).getName();
                    MessaggiResult[i][1] = (userDAO.getUser(tmp.getSender())).getSurname();
                    MessaggiResult[i][2] = tmp.getText();
                    MessaggiResult[i][3] = (userDAO.getUser(tmp.getSender())).getEmail();
                    MessaggiResult[i][4] = (userDAO.getUser(tmp.getSender())).getAvatar();
                    //System.out.println(MessaggiResult[i][0]+" "+MessaggiResult[i][1]+" "+MessaggiResult[i][2]+" "+MessaggiResult[i][3]);
                }
                
                String [] listaChat = new String [2];
                listaChat[0] = shoppingListDAO.getShoppingList(listaSelezionata).getName();
                listaChat[1] = Integer.toString(shoppingListDAO.getShoppingList(listaSelezionata).getLID());
                
                session.setAttribute("partecipantiChat", PartecipantiResult);
                session.setAttribute("messaggiChat", MessaggiResult);
                session.setAttribute("selectedList", listaSelezionata);
                session.setAttribute("listaChat", listaChat);
                //System.out.println("---------------------- LISTA SELEZIONATA è: " + session.getAttribute("selectedList"));
                session.setAttribute("passaggioServlet", true);
                request.getRequestDispatcher("chat.jsp").forward(request, response);
                break;
            case 4:
                listaScelta = Integer.parseInt(request.getParameter("listToEliminate"));
                shoppingListDAO.deleteShoppingList(listaScelta);
                if(session.getAttribute("emailSession") != null){
                    if(shoppingListDAO.getShoppingListsByOwner((String)session.getAttribute("emailSession")).isEmpty()) {
                        session.setAttribute("listaAnonimo", false);
                    }
                } else {
                    session.setAttribute("listaAnonimo", false);
                };
                response.sendRedirect("handlingListServlet?selectedList=0");
                break;
            case 5:
                listaScelta = Integer.parseInt(request.getParameter("notToShareList"));
                String emailSession = (String)session.getAttribute("emailSession");
                sharingDAO.deleteSharing(new Sharing(emailSession, listaScelta, sharingDAO.getSharing(listaScelta, emailSession).getModify(), sharingDAO.getSharing(listaScelta, emailSession).getAdd(), sharingDAO.getSharing(listaScelta, emailSession).getDelete()));
                response.sendRedirect("handlingListServlet?selectedList=0");
                break;
            default: 
                response.sendRedirect("error.jsp"); 
                break;
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * Metodo POST non implementato
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        Library library = new Library();
        
        ShoppingListDAO shoppingListDAO1 = new MySQLShoppingListDAOImpl();
        String newName = request.getParameter("newName");
        String newNote = request.getParameter("newNote");
        String newPhoto = request.getParameter("newPhoto");
        int LID = Integer.parseInt(request.getParameter("LID"));
        int LCID = Integer.parseInt(request.getParameter("LCID"));
        String ListOwner = request.getParameter("ListOwner");
        int CookieID = Integer.parseInt(request.getParameter("CookieID"));
        shoppingListDAO1.updateShoppingList(new ShoppingList(LID, newName, newNote, library.ImageControl(newPhoto), LCID, ListOwner, CookieID));
        
        response.sendRedirect("handlingListServlet?selectedList="+LID);
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
