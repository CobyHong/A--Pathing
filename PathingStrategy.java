
import java.util.function.Predicate;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.List;

interface PathingStrategy
{
   /*
    * Returns a prefix of a path from the start point to a point within reach of
    * the end point. This path is only valid ("clear") when returned, but may be
    * invalidated by movement of other entities.
    *
    * The prefix includes neither the start point nor the end point.
    */

   // start - current point.
   // end - target point to reach.
   // canPassThrough - boundaries checker.
   // withinReach - boolean if can reach certain point from at least one direction.
   // potentialNeighbors - either Diagnol or, Diagnol Cardinal, your directions.
   List<Point> computePath(Point start, Point end,
                           Predicate<Point> canPassThrough,
                           BiPredicate<Point, Point> withinReach,
                           Function<Point, Stream<Point>> potentialNeighbors);

   // DIRECTIONS FROM CURRENT POINT.
   // UP
   // DOWN
   // LEFT
   // RIGHT
   static final Function<Point, Stream<Point>> CARDINAL_NEIGHBORS = point -> Stream.<Point>builder()
         .add(new Point(point.x, point.y - 1)).add(new Point(point.x, point.y + 1)).add(new Point(point.x - 1, point.y))
         .add(new Point(point.x + 1, point.y)).build();

   static final Function<Point, Stream<Point>> DIAGONAL_CARDINAL_NEIGHBORS = point -> Stream.<Point>builder()
           .add(new Point(point.x - 1, point.y - 1)).add(new Point(point.x + 1, point.y + 1))
           .add(new Point(point.x - 1, point.y + 1)).add(new Point(point.x + 1, point.y - 1))
           .add(new Point(point.x, point.y - 1)).add(new Point(point.x, point.y + 1)).add(new Point(point.x - 1, point.y))
           .add(new Point(point.x + 1, point.y)).build();
}