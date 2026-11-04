////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by FernFlower decompiler)
////
//
//package com.radiusengine.bbb.webservices;
//
//import com.radiusengine.bbb.server.RadiusAcctImpl;
//import javax.ws.rs.POST;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.core.Response;
//
//@Path("radius")
//public class RadiusServices {
//    public RadiusServices() {
//    }
//
//    @Path("/dmsession/{id}")
//    @POST
//    public Response disconnectSession(@PathParam("id") String id) {
//        RadiusAcctImpl server = new RadiusAcctImpl();
//        server.disconnectSession(id);
//        return Response.ok().entity("Success").build();
//    }
//
//    @Path("/dmusersessions/{usename}")
//    @POST
//    public Response disconnectUser(@PathParam("usename") String usename) {
//        RadiusAcctImpl server = new RadiusAcctImpl();
//        server.disconnectUserSessions(usename);
//        return Response.ok().entity("Success").build();
//    }
//}
