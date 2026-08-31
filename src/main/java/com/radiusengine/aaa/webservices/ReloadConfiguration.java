/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.webservices;

import com.radiusengine.aaa.server.CachingConfigurations;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;

@Path("reloadConfiguration")
public class ReloadConfiguration {
    public ReloadConfiguration() {
    }

    @POST
    @Consumes({"text/plain"})
    @Produces({"text/plain"})
    public String reloadConfiguration(@Context Request request, String parameter) {
        CachingConfigurations cacheConfig = CachingConfigurations.getInstance();
        cacheConfig.reloadConfiguration();
        return "success";
    }
}
