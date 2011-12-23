package gr.infolytics.recommendations.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * This will do required initialization steps for the first time that the
 * application is launched.
 */
public class RecommendationsInitializer extends HttpServlet {

    private static final long serialVersionUID = 3144987546202208124L;


    public RecommendationsInitializer() {
        super();
    }

    public void init(ServletConfig config) throws ServletException {
    }

}
