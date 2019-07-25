package org.dbpedia.iri

import java.io.File

import org.apache.jena.iri.IRIException
import org.apache.jena.query.{QueryExecutionFactory, QueryFactory}
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.system.IRIResolver
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite

import scala.collection.JavaConversions._


case class ReduceScore(cntAll: Long, cntTrigger: Long, cntValid: Long)
case class SPO(s: String, p: String, o: String)

class IRI_Test_Suite  extends FunSuite{


  test("Trigger Test") {
    /*
    TODO
     */
  }

  test("Test Case Query") {

    val pathToTestCases = "../new_release_based_ci_tests_draft.nt"

    val model = ModelFactory.createDefaultModel()
    model.read(pathToTestCases)

    val query = QueryFactory.create(org.dbpedia.validation.testQuery())
    val queryExecutionFactory = QueryExecutionFactory.create(query,model)

    val resultSet = queryExecutionFactory.execSelect()

    val vars = resultSet.getResultVars

    while( resultSet.hasNext ) {
      val solution = resultSet.next()
      vars.foreach(v => println(solution.get(v)))
    }
  }

  test("Spark Approach") {

    val hadoopHomeDir = new File("./haoop/")
    hadoopHomeDir.mkdirs()
    System.setProperty("hadoop.home.dir", hadoopHomeDir.getAbsolutePath)
    System.setProperty("log4j.logger.org.apache.spark.SparkContext", "WARN")

    val extractionOutputTtl =
      s"""
         |<http://wikidata.dbpedia.org/resource/Q15> <http://www.georss.org/georss/point> "1.0 17.0" .
         |<http://wikidata.dbpedia.org/resource/Q15> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing> .
         |<http://wikidata.dbpedia.org/resource/Q15> <http://www.w3.org/2003/01/geo/wgs84_pos#lat> "1.0"^^<http://www.w3.org/2001/XMLSchema#float> .
         |<http://wikidata.dbpedia.org/resource/Q15> <http://www.w3.org/2003/01/geo/wgs84_pos#long> "17.0"^^<http://www.w3.org/2001/XMLSchema#float> .
         |<http://wikidata.dbpedia.org/resource/Q21> <http://www.georss.org/georss/point> "53.0 -1.0" .
         |<http://wikidata.dbpedia.org/resource/Q21> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing> .
         |<http://wikidata.dbpedia.org/resource/Q21> <http://www.w3.org/2003/01/geo/wgs84_pos#lat> "53.0"^^<http://www.w3.org/2001/XMLSchema#float> .
         |<http://wikidata.dbpedia.org/resource/Q21> <http://www.w3.org/2003/01/geo/wgs84_pos#long> "-1.0"^^<http://www.w3.org/2001/XMLSchema#float> .
         |<http://wikidata.dbpedia.org/resource/Q18> <http://www.georss.org/georss/point> "-21.0 -59.0" .
       """.stripMargin.trim

    val sparkSession = SparkSession.builder().config("hadoop.home.dir", "./hadoop")
      .appName("Dev 3").master("local[*]").getOrCreate()

    //    sparkSession.sparkContext.setLogLevel("WARN")

    val sqlContext = sparkSession.sqlContext
    import sqlContext.implicits._

    val rdd = sqlContext.createDataset(extractionOutputTtl.lines.toSeq)

    val counts = rdd.map(line => {

      val spo = line.split(" ", 3)

      //      implicit def betterStringConversion(str: String) = new BetterString(str)

      var s: String = null
      if (spo(0).startsWith("<")) {
        s = spo(0).substring(1, spo(0).length - 1)
      }

      //  var tS, vS, tP, vP, tO, vO: Long = 0L
      //
      var p: String = null
      if (spo(1).startsWith("<")) {
        p = spo(1).substring(1, spo(1).length - 1)
      }

      var o: String = null
      if (spo(2).startsWith("<")) {
        o = spo(2).substring(1, spo(2).length - 3)
      }

      println(s)
      SPO(s,p,o)
    }).map(_.s).distinct().filter(_ != null).map( x => ReduceScore(1,1,0) )
      .reduce( (a,b) => ReduceScore(a.cntAll+b.cntAll,a.cntTrigger+b.cntTrigger,a.cntValid+b.cntValid))

    println(counts.cntAll)
    println(counts.cntTrigger)
    println(counts.cntValid)


  }
  case class RawRdfTripleParts()

  case class IriScore(tS: Long , vS: Long, tP: Long, vP: Long, tO: Long, vO:Long)

  implicit  class FlatRdfTriplePart(s: String) {
    def checkIsIri: Boolean = s.startsWith("<")
  }

  test("Single Iri Parse Test") {

    try {
      IRIResolver.iriFactory.construct("http://dbpedia.org/>/test")
    }
    catch {
      case iriex: IRIException => println("Invalid IRI definition")
    }

  }

  //test("Another Test") {
  //
  //
  //  val m_tests = ModelFactory.createDefaultModel()
  //  m_tests.read("../new_release_based_ci_tests_draft.nt")
  //
  //  val q_validator = QueryFactory.create(
  //
  //  s"""
  //         |PREFIX v: $prefix_v
  //         |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
  //         |
  //         |SELECT ?validator ?hasScheme ?hasQuery ?hasFragment (group_concat(?notContain; SEPARATOR="\t") as ?notContains) {
  //         |  ?validator
  //         |     a                          v:IRI_Validator ;
  //         |     v:hasScheme                ?hasScheme ;
  //         |     v:hasQuery                 ?hasQuery ;
  //         |     v:hasFragment              ?hasFragment ;
  //         |     v:doesNotContainCharacters ?notContain .
  //         |
  //         |} GROUP BY ?validator ?hasScheme ?hasQuery ?hasFragment
  //      """.stripMargin)
  //
  //  val query_exec = QueryExecutionFactory.create(q_validator, m_tests)
  //  val result_set = query_exec.execSelect()
  //
  //  val l_iri_validator = ListBuffer[IRI_Validator]()
  //
  //  while (result_set.hasNext) {
  //
  //  val solution = result_set.next()
  //
  //  print(
  //  s"""
  //           |FOUND VALIDATOR: ${solution.getResource("validator").getURI}
  //           |> SCHEME: ${solution.getLiteral("hasScheme").getLexicalForm}
  //           |> QUERY: ${solution.getLiteral("hasQuery").getLexicalForm}
  //           |> FRAGMENT: ${solution.getLiteral("hasFragment").getLexicalForm}
  //           |> NOT CONTAIN: ${List(solution.getLiteral("notContains").getLexicalForm)}
  //        """.stripMargin
  //  )
  //}
}

