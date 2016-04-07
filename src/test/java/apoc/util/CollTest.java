package apoc.util;

import apoc.coll.Coll;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.*;

import static apoc.util.TestUtil.testCall;
import static apoc.util.TestUtil.testResult;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CollTest {

    private GraphDatabaseService db;
    @Before public void setUp() throws Exception {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();
        TestUtil.registerProcedure(db,Coll.class);
    }
    @After public void tearDown() {
        db.shutdown();
    }

    @Test public void testZip() throws Exception {
        testCall(db, "CALL apoc.coll.zip([1,2,3],[4,5])",
                (row) -> {
                    Object value = row.get("value");
                    List<List<Long>> expected = asList(asList(1L, 4L), asList(2L, 5L), asList(3L, null));
                    assertEquals(expected, value);
                });
    }
    @Test public void testPairs() throws Exception {
        testCall(db, "CALL apoc.coll.pairs([1,2,3])",
                (row) -> assertEquals(asList(asList(1L,2L),asList(2L,3L),asList(3L,null)), row.get("value")));
    }
    @Test public void testSum() throws Exception {
        testCall(db, "CALL apoc.coll.sum([1,2,3])",
                (row) -> assertEquals(6D, row.get("value")));
    }
    @Test public void testMin() throws Exception {
        testCall(db, "CALL apoc.coll.min([1,2,3])",
                (row) -> assertEquals(1L, row.get("value")));
    }
    @Test public void testMax() throws Exception {
        testCall(db, "CALL apoc.coll.max([1,2,3])",
                (row) -> assertEquals(3L, row.get("value")));
    }
    @Test public void testPartition() throws Exception {
        testResult(db, "CALL apoc.coll.partition([1,2,3,4,5],2)",
                (result) -> {
                    Map<String, Object> row = result.next();
                    assertEquals(asList(1L,2L), row.get("value"));
                    row = result.next();
                    assertEquals(asList(3L,4L), row.get("value"));
                    row = result.next();
                    assertEquals(asList(5L), row.get("value"));
                    assertFalse(result.hasNext());
                });
    }

    @Test public void testSumLongs() throws Exception {
        testCall(db, "CALL apoc.coll.sumLongs([1,2,3])",
                (row) -> assertEquals(6L, row.get("value")));
    }
    @Test public void testSort() throws Exception {
        testCall(db, "CALL apoc.coll.sort([3,2,1])",
                (row) -> assertEquals(asList(1L,2L,3L), row.get("value")));
    }

    @Test public void testIN() throws Exception {
        testCall(db, "CALL apoc.coll.contains([1,2,3],1)",
                (row) -> assertEquals(true, row.get("value")));
    }
    @Test public void testIN2() throws Exception {
        int elements = 1_000_000;
        ArrayList<Long> list = new ArrayList<>(elements);
        for (long i = 0; i< elements; i++) {
            list.add(i);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("list", list);
        params.put("value", list.get(list.size()-1));
        long start = System.currentTimeMillis();
        testCall(db, "CALL apoc.coll.contains({list},{value})", params,
                (row) -> assertEquals(true, row.get("value")));
        System.out.printf("contains test on %d elements took %d ms%n", elements, System.currentTimeMillis() - start);
    }
    @Test public void testContainsSorted() throws Exception {
        int elements = 1_000_000;
        ArrayList<Long> list = new ArrayList<>(elements);
        for (long i = 0; i< elements; i++) {
            list.add(i);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("list", list);
        params.put("value", list.get(list.size()/2));
        long start = System.currentTimeMillis();
        testCall(db, "CALL apoc.coll.containsSorted({list},{value})", params,
                (row) -> assertEquals(true, row.get("value")));
        System.out.printf("contains sorted test on %d elements took %d ms%n", elements, System.currentTimeMillis() - start);
    }

    @Test public void testSortNodes() throws Exception {
        testCall(db,
            "CREATE (n {name:'foo'}),(m {name:'bar'}) WITH n,m CALL apoc.coll.sortNodes([n,m], 'name') YIELD value RETURN value",
            (row) -> {
                List<Node> nodes = (List<Node>) row.get("value");
                assertEquals("bar", nodes.get(0).getProperty("name"));
                assertEquals("foo", nodes.get(1).getProperty("name"));
            });
    }

}
