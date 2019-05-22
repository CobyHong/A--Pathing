import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AStarPathingStrategy implements PathingStrategy {
    // start - current point.
    // end - target point to reach.
    // canPassThrough - boundaries checker.
    // withinReach - boolean if can reach certain point from at least one direction.
    // potentialNeighbors - either Diagnol or, Diagnol Cardinal, your directions.
    public List<Point> computePath(Point current, Point end, Predicate<Point> canPassThrough,
            BiPredicate<Point, Point> withinReach, Function<Point, Stream<Point>> potentialNeighbors) {
        //Path we will add to.
        List<Point> path = new ArrayList<>();
        //going back from where we were... Get good points.
        List<Point> finalPath = new ArrayList<>();
        //Closed path, also used to grab parents. key is its point.
        Map<Point, Node> closedList = new HashMap<Point, Node>();
        Map<Point, Node> closedList2 = new HashMap<Point, Node>();
        //==================================================================================================
        //Builds the ORIGINAL starting node.
        Node STARTER = new Node(0, heuristic(current,end), 0+heuristic(current,end), null, current);
        Node END_STARTER = new Node(0, heuristic(end,current), 0+heuristic(end,current), null, end);
        //add starter node to the closedMap.
        closedList.put(current, STARTER);
        //==================================================================================================
        //STARTER never changes (duh). start becomes current, end never changes (duh).
        //openMap and closedMap constantly changed.
        //RECURSIVE FUNCTION.
        //FIRST TRAVERSAL
        pathFinder(path, STARTER.position, current, end, canPassThrough, potentialNeighbors, closedList);
        //parentBack(end, current, closedList, finalPath);
        //basically same function, but going opposite direction and taking into account...
        //already traversed points. Rebuilds path but better.
        System.out.println("\n\n=========================================STARTING BACK-PATH:=========================================");
        //same initializing like first pathFinder.
        closedList2.put(end, END_STARTER);
        //its reversed for this.
        //end = our current position
        //current = our end goal
        //RECURSIVE FUNCTION.
        pathBack(finalPath, END_STARTER.position, end, current, canPassThrough, potentialNeighbors, closedList2, path);
        System.out.println("\n\n*Scroll up for full traversal steps\n\n\n\n\n\n\n\n\n\n\nFINAL PATH GENERATED:" + finalPath);
        return finalPath;
    }
    //Takes in the path made by pathfinder. We basically reverse the steps, making directions based on distance AND already traversed points only!
    private void pathBack(List<Point> path, Point END_STARTER, Point current, Point end, Predicate<Point> canPassThrough, Function<Point, Stream<Point>> potentialNeighbors, Map<Point, Node> closedList2, List<Point> oldpath)
    {
        System.out.println("\nPath: " + path);
        //openList always resets so only have to manage at most 4 directions at a time.
        Map<Point, Node> openList = new HashMap<Point, Node>();
        openList.clear();
        //updated call on directionsBack. based on new current. updates the openList.
        directionsBack(END_STARTER, current, end, potentialNeighbors, openList, closedList2, oldpath);
        System.out.println("current OpenList size: " + openList.size());
        //if already at destination, we are done.
        if(current.equals(end)) { return; }
        else if(openList.isEmpty() && !closedList2.isEmpty())
        {
            System.out.println("\nencounter stoppage. backtracking...");
            System.out.println("going back to this parent to redo path finding: " + closedList2.get(current).parent.position);
            //if null, remove current from the path.
            //this upon assumption when call !=null first.
            path.remove(current);
            //call back to parent. With, changed openList & closedList, should choose new direction.
            pathBack(path, END_STARTER, closedList2.get(current).parent.position, end, canPassThrough, potentialNeighbors, closedList2, oldpath);
        }
        else {
            System.out.println("\nFound directions...");
            //grab directions based on current.
            List<Node> best_to_worst_directions = new ArrayList<>();
            best_to_worst_directions.clear();
            //traverse openMap through key.
            for (Point p : openList.keySet()) {
                //openMap contains directions.
                //If the directions have parent node that matches current, grab it.
                //        UP
                //LEFT (parent) RIGHT (possible openMap direction)
                //       DOWN
                //current = center. openList contains direction that have parent.
                if(current.equals(openList.get(p).parent.position)) {
                    best_to_worst_directions.add(0, openList.get(p));
                }
            }
            //arrange the directions by F value (least to greatest).
            Comparator<Node> arrange_f = Comparator.comparing(Node::f);
            Comparator<Node> arrange_posx = (n1, n2) -> (n1.position.x - n2.position.x);
            Comparator<Node> arrange = arrange_f.thenComparing(arrange_posx);
            best_to_worst_directions.sort(arrange);
            System.out.println("Picking for shortest distance from listed...");
            best_to_worst_directions.forEach(node -> System.out.print(node.position + " " + node.f + " "));
            //grab the best path (from our sorted list of directions).
            Node bestPath = best_to_worst_directions.get(0);
            System.out.println("Position chose:" + bestPath.position);
            //add to path.
            path.add(0, bestPath.position);
            //add to closed list.
            closedList2.put(bestPath.position, bestPath);
            //recall function with bestPath as new current.
            pathBack(path, END_STARTER, bestPath.position, end, canPassThrough, potentialNeighbors, closedList2, oldpath);
        }
    }
    //Creates original pathing...
    //Generates directions list.
    //if null directions list. recursive call to function so current goes back.
    //if directions, jump to best directions and put into closedList (closedList handling null direction cases). So don't jump back to same spot on recursive call.
    private void pathFinder(List<Point> path, Point STARTER, Point current, Point end, Predicate<Point> canPassThrough, Function<Point, Stream<Point>> potentialNeighbors, Map<Point, Node> closedList)  {
        System.out.println("\nPath: " + path);
        //check if already on at destination and break if so.
        if(current.equals(end)) { return; }
        //openList always resets so only have to manage at most 4 directions at a time.
        Map<Point, Node> openList = new HashMap<Point, Node>();
        openList.clear();
        //updated call on directions. based on new current. updates the openList.
        directions(STARTER, current, end, canPassThrough, potentialNeighbors, openList, closedList);
        System.out.println("current OpenList size: " + openList.size());
        //if no directions...
        if(openList.isEmpty() && !closedList.isEmpty())
        {
            System.out.println("\nencounter stoppage. backtracking...");
            System.out.println("going back to this parent to redo path finding: " + closedList.get(current).parent.position);
            //if null, remove current from the path.
            //this upon assumption when call !=null first.
            path.remove(current);
            //call back to parent. With, changed openList & closedList, should choose new direction.
            pathFinder(path, STARTER, closedList.get(current).parent.position, end, canPassThrough, potentialNeighbors, closedList);
        }
        //if directions...
        if (!openList.isEmpty())
        {
            System.out.println("\nFound directions...");
            //grab directions based on current.
            List<Node> best_to_worst_directions = new ArrayList<>();
            best_to_worst_directions.clear();
            //traverse openMap through key.
            for (Point p : openList.keySet()) {
                //openMap contains directions.
                //If the directions have parent node that matches current, grab it.
                //        UP
                //LEFT (parent) RIGHT (possible openMap direction)
                //       DOWN
                //current = center. openList contains direction that have parent.
                if(current.equals(openList.get(p).parent.position))
                {
                    best_to_worst_directions.add(0, openList.get(p));
                }
            }
            //arrange the directions by F value (least to greatest).
            Comparator<Node> arrange_f = Comparator.comparing(Node::f);
            Comparator<Node> arrange_posx = (n1, n2) -> (n2.position.x - n1.position.x);
            Comparator<Node> arrange = arrange_f.thenComparing(arrange_posx);
            best_to_worst_directions.sort(arrange);
            System.out.println("Picking for shortest distance from listed...");
            best_to_worst_directions.forEach(node -> System.out.print(node.position + " " + node.f + " "));
            //grab the best path (from our sorted list of directions).
            Node bestPath = best_to_worst_directions.get(0);
            System.out.println("Position chose:" + bestPath.position);
            //add to path.
            path.add(bestPath.position);
            //add to closed list.
            closedList.put(bestPath.position, bestPath);
            //recall function with bestPath as new current.
            pathFinder(path, STARTER, bestPath.position, end, canPassThrough, potentialNeighbors, closedList);
        }
    }
    private void directions(Point STARTER, Point current, Point end, Predicate<Point> canPassThrough, Function<Point, Stream<Point>> potentialNeighbors,Map<Point, Node> openList, Map<Point, Node> closedList)  {
        //creates possible directions that DON'T HAVE OBSTACLES on them. up + down + left + right.
        //Does not take into account if direction in closedList. will do check below.
        List<Point> directions = potentialNeighbors.apply(current).filter(canPassThrough).collect(Collectors.toList());
        //convert these directions into nodes and place into openMap.
        //        UP
        //LEFT (parent) RIGHT
        //       DOWN
        //The parent is the center node. for these directions (duh).
        for(Point p : directions) {
            //if closedList doesn't contain the possible direction already (means traversed)...
            //filters out our directions so we don't choose same step again.
            if(!closedList.containsKey(p)) {
                //closedList.get(current) -> grabs node of current.
                //adds to the openList we have as parameter.
                openList.put(p, new Node(heuristic(STARTER, p), heuristic(p, end),heuristic(STARTER, p) + heuristic(p, end), closedList.get(current), p));
            }
        }
    }
    //For grabbing old Directions in terms of path we already traversed.
    private void directionsBack(Point END_STARTER, Point current, Point end, Function<Point, Stream<Point>> potentialNeighbors,Map<Point, Node> openList, Map<Point, Node> closedList2, List<Point> oldPath)  {
        //creates possible directions. don't have to check for obstacles since using old path to traverse.
        //Does not take into account if direction in closedList. will do check below.
        List<Point> directions = potentialNeighbors.apply(current).collect(Collectors.toList());
        //convert these directions into nodes and place into openMap.
        //        UP
        //LEFT (parent) RIGHT
        //       DOWN
        //The parent is the center node. for these directions (duh).
        for(Point p : directions) {
            //only get directions that are already traversed by old path.
            //also if one of the points will equal the end (since end is an object can't reach LUL.
            if(oldPath.contains(p) || p.equals(end)) {
                if(!closedList2.containsKey(p)) {
                    //closedList2.get(current) -> grabs node of current.
                    //adds to the openList we have as parameter.
                    openList.put(p, new Node(heuristic(END_STARTER, p), heuristic(p, end), heuristic(END_STARTER, p) + heuristic(p, end), closedList2.get(current), p));
                }
            }
        }
    }
    //h-value
    private static int heuristic(Point current, Point end) {
        int x = Math.abs(current.x - end.x);
        int y = Math.abs(current.y - end.y);
        return x+y;
    }
    class Node {
        private int g; //distance from start.
        private int h; //distance from goal.
        private int f; //total distance. f = g + h
        private Node parent;
        private Point position;

        public Node(int g, int h, int f, Node parent, Point position) {
            this.g = g;
            this.h = h;
            this.f = f;
            this.parent = parent;
            this.position = position;
        }
        public String toString() {
            return "[KEY:] position:" + position + " [VALUE]: g:" + g + " h:" + h + " f:" + f;
        }
        //getters
        public int g() { return g; }
        public int h() { return h; }
        public int f() { return f; }
        public Node parent() { return parent; }
        public Point position() { return position; }
    }
}
