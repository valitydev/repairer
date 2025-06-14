package dev.vality.repairer.servlet;

import dev.vality.repairer.RepairManagementSrv;
import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/v1/repair")
@RequiredArgsConstructor
public class RepairerServlet extends GenericServlet {

    private final RepairManagementSrv.Iface requestHandler;
    private Servlet thriftServlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .build(RepairManagementSrv.Iface.class, requestHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }
}
