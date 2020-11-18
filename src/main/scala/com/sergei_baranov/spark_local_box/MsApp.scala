package com.sergei_baranov.spark_local_box

//import akka.actor.typed._
//import java.util.Dictionary

//import akka.{Done, actor}
//import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

import scala.reflect.ClassTag

//import akka.actor.typed.scaladsl.adapter._

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
//import akka.http.scaladsl.model
import akka.http.scaladsl.model._
//import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.HttpEntity

//import spray.json._
// for JSON serialization/deserialization following dependency is required:
// "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7"
//import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
//import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._


//import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
//import spray.json._

import scala.io.StdIn
//import scala.io.Source
import java.io.File

import scala.sys.process._

//import scala.concurrent.Future

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import spray.json._

object MsApp {

  var confObj: Config = null
  var jobsTree: Config = null

  // needed to run the route
  implicit val system = akka.actor.typed.ActorSystem(Behaviors.empty, "MsAppInst")
  // need for http
  //implicit val untypedSystem: actor.ActorSystem = system.toUntyped
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext = system.executionContext

  // https://docs.oracle.com/javase/7/docs/technotes/tools/windows/classpath.html
  // https://spark.apache.org/docs/latest/submitting-applications.html

  /*
  def datainRoute(credUser: String, credPwd: String, anchorDate: String): Route = concat(
    //complete("jobs/datain/" + credUser + "/" + credPwd + "/" + anchorDate)
    complete {
      println("DBG: datainRoute START")
      val s = (Seq("java",
        //"-classpath", "/shara/cbc_liquidity_datain-1.0-SNAPSHOT-uber.jar",
        "-jar", "/shara/cbc_liquidity_datain-1.0-SNAPSHOT-uber.jar",
        "com.sergei_baranov.cbonds_calc_liquidity.cbc_liquidity_datain.DatainApp",
        credUser, credPwd, anchorDate).!!)
      println("DBG: datainRoute FIN")
      s
    }
  )
  def calcRoute(dbUrl: String, dbUser: String, dbPwd: String, anchorDate: String): Route = concat(
    //complete("jobs/calc/" + dbUrl + "/" + dbUser + "/" + dbPwd + "/" + anchorDate)
    complete {
      println("DBG: calcRoute START")
      val s = (Seq("/usr/local/spark/bin/spark-submit",
        "--conf", "spark.ui.enabled=false",
        "--class", "com.sergei_baranov.cbonds_calc_liquidity.cbc_liquidity_calc.CalcApp",
        "--master", "local[2]", "local:/shara/cbc_liquidity_calc-1.0-SNAPSHOT-uber.jar",
        dbUrl, dbUser, dbPwd, anchorDate).!!)
      println("DBG: calcRoute FIN")
      s
    }
  )
  */

  //
  def callJob(moduleName: String, jobName: String,
              ext1: String, ext2: String, ext3: String, ext4: String, ext5: String): Route = concat(
    complete {
      var s: String = "invalid module name"
      var moduleConfig: Config = null
      var jobConfig: Config = null
      var classTag: String = ""
      var p1: String = ""
      var p2: String = ""
      var p3: String = ""
      var p4: String = ""
      var p5: String = ""
      var isSpark: Int = 0
      var jarName: String = ""
      var appName: String = ""
      var description: String = ""

      try {
        moduleConfig = this.jobsTree.getConfig(moduleName)
        classTag = getClassTag(moduleConfig)
      } catch {
        case e: Throwable => s = Map("err" -> s).toJson.toString
      }
      if ("" != classTag) {
        s = "invalid job name"
        classTag = ""
        try {
          jobConfig = moduleConfig.getConfig(jobName)
          classTag = getClassTag(jobConfig)
        } catch {
          case e: Throwable => s = Map("err" -> s).toJson.toString
        }
      }
      if ("" != classTag) {
        try {
          isSpark = jobConfig.getInt("spark")
        } catch {
          case e: Throwable => isSpark = 0
        }
        try {
          jarName = jobConfig.getString("jar")
        } catch {
          case e: Throwable => jarName = ""
        }
        if ("" == jarName) {
          s = Map("err" -> "can not detect jar name", "description" -> description).toJson.toString
        } else {
          try {
            appName = jobConfig.getString("app")
          } catch {
            case e: Throwable => appName = ""
          }
          if ("" == appName) {
            s = Map("err" -> "can not detect app name", "description" -> description).toJson.toString
          } else {
            println(ext1 + "/" + ext2 + "/" + ext3 + "/" + ext4 + "/" + ext5)
            s = ""
            try {
              description = jobConfig.getString("description")
            } catch {
              case e: Throwable => description = ""
            }
            try {
              p1 = jobConfig.getString("p1")
            } catch {
              case e: Throwable => p1 = ""
            }
            //println("A")
            if ("" != ext1) {
              p1 = ext1
            }
            //println("B")
            if (p1.length > 8 && "required:" == p1.substring(0, 9)) {
              s = Map("err" -> p1, "description" -> description).toJson.toString
            }
            //println("C")
            if ("" == s) {
              try {
                p2 = jobConfig.getString("p2")
              } catch {
                case e: Throwable => p2 = ""
              }
              //println("D")
              if ("" != ext2) {
                p2 = ext2
              }
              //println("E")
              if (p2.length > 8 && "required:" == p2.substring(0, 9)) {
                s = Map("err" -> p2, "description" -> description).toJson.toString
              }
              //println("F")
            }
            if ("" == s) {
              try {
                p3 = jobConfig.getString("p3")
              } catch {
                case e: Throwable => p3 = ""
              }
              //println("G")
              if ("" != ext3) {
                p3 = ext3
              }
              //println("H")
              if (p3.length > 8 && "required:" == p3.substring(0, 9)) {
                s = Map("err" -> p3, "description" -> description).toJson.toString
              }
              //println("I")
            }
            if ("" == s) {
              try {
                p4 = jobConfig.getString("p4")
              } catch {
                case e: Throwable => p4 = ""
              }
              //println("J")
              if ("" != ext4) {
                p4 = ext4
              }
              //println("K")
              if (p4.length > 8 && "required:" == p4.substring(0, 9)) {
                s = Map("err" -> p4, "description" -> description).toJson.toString
              }
              //println("L")
            }
            if ("" == s) {
              try {
                p5 = jobConfig.getString("p5")
              } catch {
                case e: Throwable => p5 = ""
              }
              //println("M")
              if ("" != ext5) {
                p5 = ext5
              }
              //println("N")
              if (p5.length > 8 && "required:" == p5.substring(0, 9)) {
                s = Map("err" -> p5, "description" -> description).toJson.toString
              }
              //println("O")
            }
            //println("P")
            if ("" == s) {
              if (1 == isSpark) {
                // spark
                s = (Seq("/usr/local/spark/bin/spark-submit",
                  "--conf", "spark.ui.enabled=false",
                  "--class", appName,
                  "--master", "local[2]",
                  ("local:/shara/" + jarName), p1, p2, p3, p4, p5).!!)
                s = Map("result" -> s).toJson.toString
              } else {
                // java
                s = (Seq("java", "-jar", ("/shara/" + jarName), appName, p1, p2, p3, p4, p5).!!)
                s = Map("result" -> s).toJson.toString
              }
              //println("Q")
              //s = isSpark + "/" + jarName + "/" + appName + "/" + p1 + "/" + p2 + "/" + p3 + "/" + p4 + "/" + p5 + "/"
              //println("R")
              //s = Map("result" -> s).toJson.toString
              //println("S")
            }
          }
        }
      }
      //println("T")
      // return
      s
    }
  )

  //
  def textRespRoute(txt: String, statusCode: StatusCode = StatusCodes.NotFound): Route = concat(
    complete {
      var k = "err"
      if (StatusCodes.OK == statusCode) {
        k = "result"
      }
      val dct = Map(k -> txt)
      val re = HttpEntity(ContentTypes.`application/json`, dct.toJson.toString)
      HttpResponse(statusCode, entity = re,
        headers = List(`Content-Type`(ContentTypes.`application/json`)))
    }
  )

  def main(args: Array[String]): Unit = {
    val route: Route = concat(
      pathPrefix("jobs") {
        concat(
          pathEndOrSingleSlash {
            textRespRoute("expecting module name", StatusCodes.NotFound)
          },
          pathPrefix(Segment) { moduleName: String =>
            concat(
              pathEndOrSingleSlash {
                textRespRoute("expecting job name", StatusCodes.NotFound)
              },
              pathPrefix(Segment) { jobName: String =>
                concat(
                  pathEndOrSingleSlash {
                    callJob(moduleName, jobName, "", "", "", "", "")
                  },
                  pathPrefix(Segment) { ext1: String =>
                    concat(
                      pathEndOrSingleSlash {
                        callJob(moduleName, jobName, ext1, "", "", "", "")
                      },
                      pathPrefix(Segment) { ext2: String =>
                        concat(
                          pathEndOrSingleSlash {
                            callJob(moduleName, jobName, ext1, ext2, "", "", "")
                          },
                          pathPrefix(Segment) { ext3: String =>
                            concat(
                              pathEndOrSingleSlash {
                                callJob(moduleName, jobName, ext1, ext2, ext3, "", "")
                              },
                              pathPrefix(Segment) { ext4: String =>
                                concat(
                                  pathEndOrSingleSlash {
                                    callJob(moduleName, jobName, ext1, ext2, ext3, ext4, "")
                                  },
                                  pathPrefix(Segment) { ext5: String =>
                                    concat(
                                      pathEndOrSingleSlash {
                                        callJob(moduleName, jobName, ext1, ext2, ext3, ext4, ext5)
                                      },
                                      pathPrefix(Segment) { ext6: String =>
                                        callJob(moduleName, jobName, ext1, ext2, ext3, ext4, ext5)
                                      },
                                      callJob(moduleName, jobName, ext1, ext2, ext3, ext4, ext5)
                                    )
                                  },
                                  callJob(moduleName, jobName, ext1, ext2, ext3, ext4, "")
                                )
                              },
                              callJob(moduleName, jobName, ext1, ext2, ext3, "", "")
                            )
                          },
                          callJob(moduleName, jobName, ext1, ext2, "", "", "")
                        )
                      },
                      callJob(moduleName, jobName, ext1, "", "", "", "")
                    )
                  },
                  callJob(moduleName, jobName, "", "", "", "", "")
                )
              },
              textRespRoute("expecting job name", StatusCodes.NotFound)
            )
          },
          textRespRoute("expecting module name", StatusCodes.NotFound)
        )
      },
      pathPrefix("ping") {
        concat(
          pathEndOrSingleSlash {
            textRespRoute("pong", StatusCodes.OK)
          },
          textRespRoute("pong", StatusCodes.OK)
        )
      },
      pathPrefix("health") {
        concat(
          pathEndOrSingleSlash {
            textRespRoute("ok", StatusCodes.OK)
          },
          textRespRoute("ok", StatusCodes.OK)
        )
      },
      textRespRoute("expecting 'jobs' or 'ping' or 'health' path prefix", StatusCodes.NotFound)
    )

    // конфигурацию в память
    val ConfFile = new File("/shara/spark_local_box.conf")
    //val ConfFile = new File("C:/VCS/github-sergei-baranov/spark_local_box/shara/spark_local_box.conf")
    /*
    val confSource = Source.fromFile("/shara/spark_local_box.conf")("UTF-8")
    val confLines = confSource.getLines()
    confSource.close()
     */
    this.confObj = ConfigFactory.parseFile(ConfFile)
    // запускаем сервер
    val msListenInternalHost = this.confObj.getString("ms.listen_internal.host")
    val msListenInternalPort = this.confObj.getInt("ms.listen_internal.port")
    val jobsS = "jobs"
    this.jobsTree = this.confObj.getConfig(jobsS)
    val bindingFuture = Http()(system).newServerAt(msListenInternalHost, msListenInternalPort).bind(route)
    println(s"Server online at http://"+ msListenInternalHost +":"+ msListenInternalPort +"/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  def getClassTag[T](v: T)(implicit ev: ClassTag[T]) = ev.toString
}