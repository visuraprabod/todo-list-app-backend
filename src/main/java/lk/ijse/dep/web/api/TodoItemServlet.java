package lk.ijse.dep.web.api;

import lk.ijse.dep.web.dto.TodoItemDTO;
import lk.ijse.dep.web.util.Priority;
import lk.ijse.dep.web.util.Status;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.json.Json;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@WebServlet(name = "TodoItemServlet", urlPatterns = "/api/v1/items/*")
public class TodoItemServlet extends HttpServlet {
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
        TodoItemDTO item = jsonb.fromJson(request.getReader(), TodoItemDTO.class);
        if(item.getId()==null || item.getText()==null || item.getUsername()==null
                ||item.getText().trim().isEmpty() || item.getUsername().trim().isEmpty()){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try(Connection connection = cp.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement(" UPDATE todo_item SET text=?,priority=?,status=? WHERE username=? AND id=?");
            pstm.setObject(1,item.getText());
            pstm.setObject(2,String.valueOf(item.getPriority()));
            pstm.setObject(3,String.valueOf(item.getStatus()));
            pstm.setObject(4,item.getUsername());
            pstm.setObject(5,item.getId());

            if(pstm.executeUpdate()>0){
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else{
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }


        } catch (SQLException throwables) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throwables.printStackTrace();
        }


    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String user = (String) req.getAttribute("user");

        String id = req.getParameter("id");
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try(Connection connection = cp.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM todo_item WHERE username=? AND id=?");
            pstm.setObject(1,user);
            pstm.setObject(2,id);

            if(pstm.executeUpdate()>0){
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else{
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException throwables) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throwables.printStackTrace();
        }


    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
        TodoItemDTO item = jsonb.fromJson(request.getReader(), TodoItemDTO.class);
        if(item.getId()!=null || item.getText()==null || item.getUsername()==null
        ||item.getText().trim().isEmpty() || item.getUsername().trim().isEmpty()){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;


        }

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try(Connection connection = cp.getConnection()){
             PreparedStatement pstm = connection.prepareStatement("SELECT * FROM user WHERE username=?");
             pstm.setObject(1,item.getUsername());
             if(!pstm.executeQuery().next()){
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 response.getWriter().println("Invalid User");
             }

             pstm = connection.prepareStatement("INSERT INTO todo_item (`text`,`priority`,`status`,`username`) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                     pstm.setObject(1,item.getText());
             pstm.setObject(2,item.getPriority().toString());
             pstm.setObject(3,item.getStatus().toString());
             pstm.setObject(4,item.getUsername());

             if(pstm.executeUpdate()>0){
                 response.setStatus(HttpServletResponse.SC_CREATED);
                 ResultSet generatedKeys = pstm.getGeneratedKeys();
                 generatedKeys.next();
                 int generatedId = generatedKeys.getInt(1);
                 item.setId(generatedId);
                 response.setContentType("application/json");
                 response.getWriter().println(jsonb.toJson(item));
             }else{
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        request.setAttribute("user","visura");
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");


        if(request.getPathInfo() ==null){
            PreparedStatement pstm;
            try (Connection connection = cp.getConnection()) {
                pstm = connection.prepareStatement("SELECT * FROM todo_item WHERE username=?");
                List<TodoItemDTO> todoItemDTOList = new ArrayList<>();
                pstm.setObject(1,request.getAttribute("user"));
                ResultSet rst = pstm.executeQuery();
                while(rst.next()){
                    todoItemDTOList.add(new TodoItemDTO(rst.getInt("id"),rst.getString("text"), Priority.valueOf(rst.getString("priority")), Status.valueOf(rst.getString("status")),rst.getString("username")));
                }
                response.getWriter().println(JsonbBuilder.create().toJson(todoItemDTOList));
                response.setStatus(200);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }else{
            try(Connection connection=cp.getConnection()){
                int id = Integer.parseInt(request.getPathInfo().replace("/", ""));
                System.out.println(id);
                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM todo_item WHERE id=? AND username=?");
                pstm.setObject(1,id);
                pstm.setObject(2,request.getAttribute("user"));
                ResultSet rst = pstm.executeQuery();
                if(rst.next()){
                    TodoItemDTO item = new TodoItemDTO(rst.getInt("id"), rst.getString("text"), Priority.valueOf(rst.getString("priority")), Status.valueOf(rst.getString("status")), rst.getString("username"));
                    response.setContentType("application/json");
                    response.getWriter().println(JsonbBuilder.create().toJson(item));
                }else{
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }catch (NumberFormatException ex){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                ex.printStackTrace();
                System.out.println("Invalid Id");
            } catch (SQLException throwables) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                throwables.printStackTrace();
            }
        }





    }
}
