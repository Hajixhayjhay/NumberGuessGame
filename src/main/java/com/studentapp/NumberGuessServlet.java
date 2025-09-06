package com.studentapp;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NumberGuessServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private int targetNumber;
    @Override
    public void init() throws ServletException {
        targetNumber = new Random().nextInt(100) + 1;
    }
    // ===== Public setter for testing =====
    public void setTargetNumber(int number) {
        this.targetNumber = number;
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Number Guessing Game</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background-color: #F0F8FF; text-align: center; }");
        out.println("h1 { color: #333; margin-top: 50px; }");
        out.println("form { margin-top: 20px; }");
        out.println("input[type='text'] { padding: 8px; font-size: 16px; width: 150px; }");
        out.println("input[type='submit'] { padding: 8px 15px; font-size: 16px; cursor: pointer; }");
        out.println(".message { margin-top: 20px; font-size: 18px; color: #555; }");
        out.println("a { display: inline-block; margin-top: 15px; text-decoration: none; color: #007BFF; }");
        out.println("a:hover { text-decoration: underline; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Number Guessing Game</h1>");
        out.println("<form action='guess' method='post'>");
        out.println("<input type='text' name='guess' placeholder='Enter 1-100' />");
        out.println("<input type='submit' value='Submit' />");
        out.println("</form>");
        out.println("</body>");
        out.println("</html>");
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Number Guessing Game</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background-color: #F0F8FF; text-align: center; }");
        out.println("h1 { color: #333; margin-top: 50px; }");
        out.println(".message { margin-top: 20px; font-size: 18px; }");
        out.println(".low { color: blue; }");
        out.println(".high { color: red; }");
        out.println(".correct { color: green; font-weight: bold; }");
        out.println("a { display: inline-block; margin-top: 15px; text-decoration: none; color: #007BFF; }");
        out.println("a:hover { text-decoration: underline; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Number Guessing Game</h1>");
        try {
            int guess = Integer.parseInt(request.getParameter("guess"));
            if (guess < 1 || guess > 100) {
                out.println("<div class='message'>Please enter a number between 1 and 100.</div>");
            } else if (guess < targetNumber) {
                out.println("<div class='message low'>Your guess is too low. Try again!</div>");
            } else if (guess > targetNumber) {
                out.println("<div class='message high'>Your guess is too high. Try again!</div>");
            } else {
                out.println("<div class='message correct'>:tada: Congratulations! You guessed the number!</div>");
                targetNumber = new Random().nextInt(100) + 1; // Reset game
            }
        } catch (NumberFormatException e) {
            out.println("<div class='message'>Invalid input. Please enter a valid number.</div>");
        }
        out.println("<a href='guess'>Play Again</a>");
        out.println("</body>");
        out.println("</html>");
    }
}