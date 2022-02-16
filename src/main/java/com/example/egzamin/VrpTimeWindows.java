package com.example.egzamin;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.IntVar;
import com.google.ortools.constraintsolver.RoutingDimension;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;
import com.google.ortools.constraintsolver.main;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/** VRPTW. */
public class VrpTimeWindows {
    private static final Logger logger = Logger.getLogger(VrpTimeWindows.class.getName());
    static class DataModel {
        public long[][] timeMatrix;
        public final int vehicleNumber = 1;
        public final int depot = 0;
        DataModel(long[][] matrix)
        {
            this.timeMatrix = matrix;
        }
    }

    /// @brief Print the solution.
    static List<Long> printSolution(
            DataModel data, RoutingModel routing, RoutingIndexManager manager, Assignment solution) {

        List<Long> longs = new ArrayList<>();
        // Solution cost.
        logger.info("Objective : " + solution.objectiveValue());
        // Inspect solution.
        RoutingDimension timeDimension = routing.getMutableDimension("Time");
        long totalTime = 0;
        List<String> returnlist = new ArrayList<>();
        for (int i = 0; i < data.vehicleNumber; ++i) {
            long index = routing.start(i);
            logger.info("Route for Vehicle " + i + ":");
            String route = "";
            while (!routing.isEnd(index)) {
                IntVar timeVar = timeDimension.cumulVar(index);
                route += manager.indexToNode(index) + " Time(" + solution.min(timeVar)/60 + ","
                        + solution.max(timeVar)/60 + ") -> ";
                longs.add((long) manager.indexToNode(index));
                index = solution.value(routing.nextVar(index));
            }
            IntVar timeVar = timeDimension.cumulVar(index);
            route += manager.indexToNode(index) + " Time(" + solution.min(timeVar)/60 + ","
                    + solution.max(timeVar)/60 + ")";
            longs.add((long) manager.indexToNode(index));
            logger.info(route);
            String dst = "Time of the route: " + solution.min(timeVar)/60 + "min";
            logger.info(dst);
            totalTime += solution.min(timeVar);
            returnlist.add(route);
            returnlist.add(dst);
        }
        //longs.add(totalTime/60);
        logger.info("Total time of all routes: " + totalTime/60 + "min");
        //return returnlist;
        return longs;
    }

    public static List<Long> CalculateMatrixTime(long[][] matrix) throws Exception {
        Loader.loadNativeLibraries();
        // Instantiate the data problem.
        final DataModel data = new DataModel(matrix);

        // Create Routing Index Manager
        RoutingIndexManager manager =
                new RoutingIndexManager(data.timeMatrix.length, data.vehicleNumber, data.depot);

        // Create Routing Model.
        RoutingModel routing = new RoutingModel(manager);

        // Create and register a transit callback.
        final int transitCallbackIndex =
                routing.registerTransitCallback((long fromIndex, long toIndex) -> {
                    // Convert from routing variable Index to user NodeIndex.
                    int fromNode = manager.indexToNode(fromIndex);
                    int toNode = manager.indexToNode(toIndex);
                    return data.timeMatrix[fromNode][toNode];
                });

        // Define cost of each arc.
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        // Add Time constraint.
        routing.addDimension(transitCallbackIndex, // transit callback
                0, // allow waiting time
                999999999, // vehicle maximum capacities
                false, // start cumul to zero
                "Time");
        RoutingDimension timeDimension = routing.getMutableDimension("Time");
        // Add time window constraints for each location except depot.
        for (int i = 1; i < data.timeMatrix.length; ++i) {
            long index = manager.nodeToIndex(i);
            timeDimension.cumulVar(index).setRange(0, 999999999);
        }
        // Add time window constraints for each vehicle start node.
        for (int i = 0; i < data.vehicleNumber; ++i) {
            long index = routing.start(i);
            timeDimension.cumulVar(index).setRange(0, 999999999);
        }

        // Instantiate route start and end times to produce feasible times.
        for (int i = 0; i < data.vehicleNumber; ++i) {
            routing.addVariableMinimizedByFinalizer(timeDimension.cumulVar(routing.start(i)));
            routing.addVariableMinimizedByFinalizer(timeDimension.cumulVar(routing.end(i)));
        }

        // Setting first solution heuristic.
        RoutingSearchParameters searchParameters =
                main.defaultRoutingSearchParameters()
                        .toBuilder()
                        .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                        .build();

        // Solve the problem.
        Assignment solution = routing.solveWithParameters(searchParameters);

        // Print solution on console.
       return  printSolution(data, routing, manager, solution);
    }
}