package de.sekmi.histream.i2b2.services;

import java.io.InputStream;
import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/i2b2/services/WorkplaceService")
public class WorkplaceService {
	private static final Logger log = Logger.getLogger(WorkplaceService.class.getName());

	@POST
	@Path("getFoldersByUserId")
	public Response getFoldersByUserId(){
		InputStream xml = getClass().getResourceAsStream("/templates/work/folders.xml");
		if( xml == null ){
			log.warning("folders.xml not found");
		}else{
			log.info("folders");
		}
		return Response.ok(xml).build();
	}
}
