/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.reporting;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Formatter;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;
import jsprit.core.problem.vehicle.PenaltyVehicleType;

/**
 * Printer to print the details of a vehicle-routing-problem solution.
 * 
 * @author stefan schroeder
 *
 */
public class SolutionPrinter {
	
	/**
	 * Enum to indicate verbose-level.
	 * 
	 * <p> Print.CONCISE and Print.VERBOSE are available.
	 * 
	 * @author stefan schroeder
	 *
	 */
	public enum Print {
		
		CONCISE,VERBOSE 
	}
	
	/**
	 * Prints costs and #vehicles to stdout (System.out.println).
	 * 
	 * @param solution the solution to be printed
	 */
	public static void print(VehicleRoutingProblemSolution solution){
		System.out.println("[costs="+solution.getCost() + "]");
		System.out.println("[#vehicles="+solution.getRoutes().size() + "]");
		
	}
	
	private static class Jobs {
		int nServices;
		int nShipments;
		public Jobs(int nServices, int nShipments) {
			super();
			this.nServices = nServices;
			this.nShipments = nShipments;
		}
	}
	
	public static void printToFile(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution, Print print, Formatter fmtFile){
		String leftAlign = "| %-13s | %-8s | %n";
		
		fmtFile.format("+--------------------------+%n");
		fmtFile.format("| problem                  |%n");
		fmtFile.format("+---------------+----------+%n");
		fmtFile.format("| indicator     | value    |%n");
		fmtFile.format("+---------------+----------+%n");
		
		fmtFile.format(leftAlign, "noJobs", problem.getJobs().values().size());
		Jobs jobs = getNuOfJobs(problem);
		fmtFile.format(leftAlign, "noServices",jobs.nServices);
		fmtFile.format(leftAlign, "noShipments",jobs.nShipments);
		fmtFile.format(leftAlign, "fleetsize",problem.getFleetSize().toString());
		fmtFile.format("+--------------------------+%n");
		
		
		String leftAlignSolution = "| %-13s | %-40s | %n";
		fmtFile.format("+----------------------------------------------------------+%n");
		fmtFile.format("| solution                                                 |%n");
		fmtFile.format("+---------------+------------------------------------------+%n");
		fmtFile.format("| indicator     | value                                    |%n");
		fmtFile.format("+---------------+------------------------------------------+%n");
		fmtFile.format(leftAlignSolution, "costs",solution.getCost());
		fmtFile.format(leftAlignSolution, "noVehicles",solution.getRoutes().size());
        fmtFile.format(leftAlignSolution, "unassgndJobs", solution.getUnassignedJobs().size());
		fmtFile.format("+----------------------------------------------------------+%n");
		
		
	}
	
	public static void print(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution, Print print, String output_dir){
		String leftAlign = "| %-13s | %-8s | %n";
		
		System.out.format("+--------------------------+%n");
		System.out.printf("| problem                  |%n");
		System.out.format("+---------------+----------+%n");
		System.out.printf("| indicator     | value    |%n");
		System.out.format("+---------------+----------+%n");
		
		System.out.format(leftAlign, "noJobs", problem.getJobs().values().size());
		Jobs jobs = getNuOfJobs(problem);
		System.out.format(leftAlign, "noServices",jobs.nServices);
		System.out.format(leftAlign, "noShipments",jobs.nShipments);
		System.out.format(leftAlign, "fleetsize",problem.getFleetSize().toString());
		System.out.format("+--------------------------+%n");
		
		
		String leftAlignSolution = "| %-13s | %-40s | %n";
		System.out.format("+----------------------------------------------------------+%n");
		System.out.printf("| solution                                                 |%n");
		System.out.format("+---------------+------------------------------------------+%n");
		System.out.printf("| indicator     | value                                    |%n");
		System.out.format("+---------------+------------------------------------------+%n");
		System.out.format(leftAlignSolution, "costs",solution.getCost());
		System.out.format(leftAlignSolution, "noVehicles",solution.getRoutes().size());
        System.out.format(leftAlignSolution, "unassgndJobs", solution.getUnassignedJobs().size());
		System.out.format("+----------------------------------------------------------+%n");
		
		if(print.equals(Print.VERBOSE)){
			printVerboseModified(problem,solution, output_dir);
		}
	}

	private static void printVerbose(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution) {
        String leftAlgin = "| %-7s | %-20s | %-21s | %-15s | %-15s | %-15s | %-15s |%n";
        System.out.format("+--------------------------------------------------------------------------------------------------------------------------------+%n");
        System.out.printf("| detailed solution                                                                                                              |%n");
        System.out.format("+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+%n");
        System.out.printf("| route   | vehicle              | activity              | job             | arrTime         | endTime         | costs           |%n");
        int routeNu = 1;
        for(VehicleRoute route : solution.getRoutes()){
            System.out.format("+---------+----------------------+-----------------------+-----------------+-----------------+-----------------+-----------------+%n");
            double costs = 0;
            System.out.format(leftAlgin, routeNu, getVehicleString(route), route.getStart().getName(), "-", "undef", Math.round(route.getStart().getEndTime()),Math.round(costs));
            TourActivity prevAct = route.getStart();
            for(TourActivity act : route.getActivities()){
                String jobId;
                if(act instanceof JobActivity) jobId = ((JobActivity)act).getJob().getId();
                else jobId = "-";
                double c = problem.getTransportCosts().getTransportCost(prevAct.getLocation(), act.getLocation(), prevAct.getEndTime(), route.getDriver(), route.getVehicle());
                c+= problem.getActivityCosts().getActivityCost(act, act.getArrTime(), route.getDriver(), route.getVehicle());
                costs+=c;
                System.out.format(leftAlgin, routeNu, getVehicleString(route), act.getName(), jobId, Math.round(act.getArrTime()), Math.round(act.getEndTime()),Math.round(costs));
                prevAct=act;
            }
            double c = problem.getTransportCosts().getTransportCost(prevAct.getLocation(), route.getEnd().getLocation(), prevAct.getEndTime(), route.getDriver(), route.getVehicle());
            c+= problem.getActivityCosts().getActivityCost(route.getEnd(), route.getEnd().getArrTime(), route.getDriver(), route.getVehicle());
            costs+=c;
            System.out.format(leftAlgin, routeNu, getVehicleString(route), route.getEnd().getName(), "-", Math.round(route.getEnd().getArrTime()), "undef", Math.round(costs));
            routeNu++;
        }
        System.out.format("+--------------------------------------------------------------------------------------------------------------------------------+%n");
        if(!solution.getUnassignedJobs().isEmpty()) {
            System.out.format("+----------------+%n");
            System.out.format("| unassignedJobs |%n");
            System.out.format("+----------------+%n");
            String unassignedJobAlgin = "| %-14s |%n";
            for (Job j : solution.getUnassignedJobs()) {
                System.out.format(unassignedJobAlgin, j.getId());
            }
            System.out.format("+----------------+%n");
        }
	}
	
	private static void printVerboseModified(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution, String output_dir) {
		Formatter fmtCon = new Formatter(System.out);
		
	    Formatter fmtFile = null;
	    try {
			fmtFile = new Formatter(new FileOutputStream(output_dir +  "//detailed_output.fmt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		printToFile(problem, solution, Print.VERBOSE, fmtFile);
        String leftAlgin = "| %-7s | %-10s | %-15s | %-10s | %-31s | %-10s | %-10s | %-10s |%n";
        fmtFile.format("+--------------------------------------------------------------------------------------------------------------------------------+%n");
        fmtFile.format("| detailed solution                                                                                                              |%n");
        fmtFile.format("+---------+----------------+------------------+-----------------+-----------------+-----------------+-----------------+%n");
        fmtFile.format("| route   | vehicle    | activity        | job        | location                        | arrTime    | endTime    | costs      |%n");
        int routeNu = 1;
        for(VehicleRoute route : solution.getRoutes()){
            fmtFile.format("+---------+----------------------+-----------------------+-----------------+-------------------------+--------------------+-----------------+-----------------+%n");
            double costs = 0;
            fmtFile.format(leftAlgin, routeNu, getVehicleString(route), route.getStart().getName(), "-", route.getStart().getLocation().getCoordinate().toString(), "undef",  Math.round(route.getStart().getEndTime()),Math.round(costs));
            TourActivity prevAct = route.getStart();
            for(TourActivity act : route.getActivities()){
                String jobId;
                
                if(act instanceof JobActivity) jobId = ((JobActivity)act).getJob().getId();
                else jobId = "-";
                double c = problem.getTransportCosts().getTransportCost(prevAct.getLocation(), act.getLocation(), prevAct.getEndTime(), route.getDriver(), route.getVehicle());
                c+= problem.getActivityCosts().getActivityCost(act, act.getArrTime(), route.getDriver(), route.getVehicle());
                costs+=c;
                fmtFile.format(leftAlgin, routeNu, getVehicleString(route), act.getName(), jobId, act.getLocation().getCoordinate().toString(), Math.round(act.getArrTime()), Math.round(act.getEndTime()),Math.round(costs));
                prevAct=act;
            }
            double c = problem.getTransportCosts().getTransportCost(prevAct.getLocation(), route.getEnd().getLocation(), prevAct.getEndTime(), route.getDriver(), route.getVehicle());
            c+= problem.getActivityCosts().getActivityCost(route.getEnd(), route.getEnd().getArrTime(), route.getDriver(), route.getVehicle());
            costs+=c;
            fmtFile.format(leftAlgin, routeNu, getVehicleString(route), route.getEnd().getName(), "-", "", Math.round(route.getEnd().getArrTime()), "undef", Math.round(costs));
            routeNu++;
        }
        fmtFile.format("+--------------------------------------------------------------------------------------------------------------------------------+%n");
        if(!solution.getUnassignedJobs().isEmpty()) {
            fmtFile.format("+----------------+%n");
            fmtFile.format("| unassignedJobs |%n");
            fmtFile.format("+----------------+%n");
            String unassignedJobAlgin = "| %-14s |%n";
            for (Job j : solution.getUnassignedJobs()) {
                fmtFile.format(unassignedJobAlgin, j.getId());
            }
            fmtFile.format("+----------------+%n");
        }
        fmtFile.close();
	}

    private static String getVehicleString(VehicleRoute route) {
        if(route.getVehicle().getType() instanceof PenaltyVehicleType){
            return route.getVehicle().getId()+"*";
        }
        return route.getVehicle().getId();
    }

	private static Jobs getNuOfJobs(VehicleRoutingProblem problem) {
		int nShipments = 0;
		int nServices = 0;
		for(Job j : problem.getJobs().values()){
			if(j instanceof Shipment) nShipments++;
			if(j instanceof Service) nServices++;
		}
		return new Jobs(nServices,nShipments);
	}
	
}
