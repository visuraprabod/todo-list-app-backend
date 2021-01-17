package lk.ijse.dep.web.api;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lk.ijse.dep.web.dto.UserDTO;
import lk.ijse.dep.web.util.AppUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.crypto.SecretKey;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import static javax.servlet.http.HttpServletResponse.*;

@WebServlet(name = "UserServlet", urlPatterns = {"/api/v1/users/*", "/api/v1/auth"})
public class UserServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        try (Connection connection = cp.getConnection()) {
            Jsonb jsonb = JsonbBuilder.create();
            UserDTO userDTO = jsonb.fromJson(request.getReader(), UserDTO.class);
            System.out.println(userDTO);
            String s = DigestUtils.sha256Hex(userDTO.getPassword());
//        System.out.println(s);

            if (request.getServletPath().equals("/api/v1/auth")) {
                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM `user` WHERE username=?");
                pstm.setObject(1,userDTO.getUsername());
                ResultSet rst = pstm.executeQuery();
                if(rst.next()){
                    String sha256Hex = DigestUtils.sha256Hex(userDTO.getPassword());
                    if(rst.getString("password").equals(sha256Hex)){
                        String secretKey = AppUtil.getAppSecretKey();
                        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
                        System.out.println(key);
                        String jws = Jwts.builder()
                                .setIssuer("ijse")
                                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24)))
                                .setIssuedAt(new java.util.Date())
                                .claim("name", userDTO.getUsername())
                                .signWith(key)
                                .compact();
                        response.setContentType("text/plain");
                        response.getWriter().println(jws);


                        response.setStatus(SC_ACCEPTED);
                    }else{
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }else{
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);

                }

            } else {

                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM  user WHERE username=?");
                pstm.setObject(1, userDTO.getUsername());
                ResultSet rst = pstm.executeQuery();
                if (rst.next()) {
                    response.setStatus(SC_BAD_REQUEST);
                    return;
                }

                pstm = connection.prepareStatement("INSERT INTO `user` VALUES (?,?)");
                pstm.setObject(1, userDTO.getUsername());
                pstm.setObject(2, s);
                if (pstm.executeUpdate() > 0) {
                    response.setStatus(201);
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (JsonbException ex) {
            response.setStatus(SC_BAD_REQUEST);
        }


    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
