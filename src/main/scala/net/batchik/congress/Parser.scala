package net.batchik.congress


import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

import scala.collection.JavaConverters._

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.apache.commons.io.filefilter.NameFileFilter
import play.api.libs.json._
import play.api.libs.functional.syntax._

import DataTypes._
import DataTypes.Party._
import DataTypes.Gender._

object Parser {
  private final val filter = TrueFileFilter.INSTANCE

  implicit object PartyReads extends Reads[Party] {
    def reads(json: JsValue) = json match {
      case JsString("Democrat") => JsSuccess(Democrat)
      case JsString("Republican") => JsSuccess(Republican)
      case JsString("Independent") => JsSuccess(Independent)
      case _ => JsError("expected supported party")
    }
  }

  implicit object GenderReads extends Reads[Gender] {
    def reads(json: JsValue) = json match {
      case JsString("M") => JsSuccess(Male)
      case JsString("F") => JsSuccess(Female)
      case _ => JsError("expected gender")
    }
  }

  implicit object InstantReads extends Reads[Instant] {
    def reads(json: JsValue) = json match {
      case JsString(t) => JsSuccess(Instant.parse(t))
      case _ => JsError("expected date")
    }
  }

  implicit object SponsorReads extends Reads[Sponsor] {
    def reads(json: JsValue) = json match {
      case obj: JsObject => JsSuccess(Sponsor(obj.value("bioguide_id").as[String]))
      case _ => JsError("expected sponsor")
    }
  }

  implicit val personRead: Reads[Person] = (
    (JsPath \ "id" \ "bioguide").read[String] and
    (JsPath \ "name" \ "first").read[String] and
    (JsPath \ "name" \ "last").read[String] and
    (JsPath \ "terms" \ 0 \ "party").read[Party] and
    (JsPath \ "terms" \ 0 \ "state").read[String] and
    (JsPath \ "bio" \ "gender").read[Gender] and
    (JsPath \ "bio" \ "religion").readNullable[String])(Person.apply _)
  
  implicit val billRead: Reads[BillJson] = (
    (JsPath \ "bill_id").read[String] and
    (JsPath \ "sponsor").read[Sponsor] and
    (JsPath \ "cosponsors").read[Seq[Sponsor]] and
    (JsPath \ "subjects_top_term").read[String] and
    (JsPath \ "summary" \ "date").read[Instant] and
    (JsPath \ "summary" \ "text").read[String])(BillJson.apply _)


  private def parseBill(file: File): Option[BillJson] = try {
    val json = Json.parse(FileUtils.readFileToString(file, Charset.forName("UTF-8")))
    Some(json.as[BillJson])
  } catch {
    case t: Throwable => None
  }

  //------------------------------------------------------------------------------------

  def parseBills(base: File): List[BillJson] = 
    FileUtils.listFiles(base, filter, filter).asScala
    .filter(f => f.getName == "data.json")
    .filter(f => f.getPath.contains("/bills/"))
    .toArray
    .par
    .flatMap(parseBill)
    .toList

  def parsePeople(base: File): List[Person] = {
    val input = FileUtils.readFileToString(base, Charset.forName("UTF-8"))
    Json.parse(input).as[List[Person]]
  }


    
}
