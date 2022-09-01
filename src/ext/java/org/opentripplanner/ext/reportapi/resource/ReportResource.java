package org.opentripplanner.ext.reportapi.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.opentripplanner.ext.reportapi.model.BicyleSafetyReport;
import org.opentripplanner.ext.reportapi.model.TransfersReport;
import org.opentripplanner.model.transfer.TransferService;
import org.opentripplanner.standalone.api.OtpServerRequestContext;
import org.opentripplanner.transit.service.TransitService;

@Path("/report")
@Produces(MediaType.TEXT_PLAIN)
public class ReportResource {

  private final TransferService transferService;
  private final TransitService transitService;

  @SuppressWarnings("unused")
  public ReportResource(@Context OtpServerRequestContext requestContext) {
    this.transferService = requestContext.transitService().getTransferService();
    this.transitService = requestContext.transitService();
  }

  @GET
  @Path("/transfers.csv")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public String getTransfersAsCsv() {
    return TransfersReport.export(transferService.listAll(), transitService);
  }

  @GET
  @Path("/bicycle-safety.html")
  @Produces(MediaType.TEXT_HTML)
  public Response getBicycleSafetyPage() {
    var is = getClass().getResourceAsStream("/reportapi/report.html");
    try {
      return Response.ok(new String(is.readAllBytes(), StandardCharsets.UTF_8)).build();
    } catch (IOException e) {
      return Response.serverError().build();
    }
  }

  @GET
  @Path("/bicycle-safety.csv")
  @Produces("text/csv")
  public Response getBicycleSafetyAsCsv(
    @DefaultValue("default") @QueryParam("osmWayPropertySet") String osmWayPropertySet
  ) {
    return Response
      .ok(BicyleSafetyReport.makeCsv(osmWayPropertySet))
      .header(
        "Content-Disposition",
        "attachment; filename=\"" + osmWayPropertySet + "-bicycle-safety.csv\""
      )
      .build();
  }
}
