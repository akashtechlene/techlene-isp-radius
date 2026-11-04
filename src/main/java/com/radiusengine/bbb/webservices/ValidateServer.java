//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.radiusengine.bbb.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.radiusengine.bbb.server.RadiusServerImpl;
import java.util.HashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.util.RadiusServer;

@Path("validateServer")
public class ValidateServer {
    Logger log = LoggerFactory.getLogger("radiuslog");

    public ValidateServer() {
    }

    @POST
    @Consumes({"text/plain"})
    @Produces({"application/json"})
    public String validateServer(@Context Request request, String parameter) {
        RadiusServer server = new RadiusServerImpl();
        HashMap returnMap = new HashMap();
        Gson gsonBuilder = (new GsonBuilder()).create();
        returnMap.put("authPort", server.getAuthPort());
        returnMap.put("accttPort", server.getAcctPort());
        returnMap.put("status", "1");
        String jsonFromJavaMap = gsonBuilder.toJson(returnMap);
        this.log.debug("jsonFromJavaMap:" + jsonFromJavaMap);
        return jsonFromJavaMap;
    }
}
