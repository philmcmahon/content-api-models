package com.gu.contentapi.json

import com.gu.contentapi.client.model.v1._

import com.twitter.scrooge.{ThriftStruct, ThriftEnum}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime
import org.json4s.JValue
import org.json4s.JsonAST.JValue
import org.json4s._
import com.gu.storypackage.model.v1.{ArticleType, Group}
import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.quiz._
import com.gu.contentatom.thrift.atom.viewpoints._

import scala.reflect.ClassTag
import scala.util.{Try, Success, Failure}

object Serialization {

  implicit val formats = DefaultFormats +
    AtomSerializer +
    ContentTypeSerializer +
    DateTimeSerializer +
    MembershipTierSerializer +
    OfficeSerializer +
    AssetTypeSerializer +
    ElementTypeSerializer +
    TagTypeSerializer +
    SponsorshipTypeSerializer +
    CrosswordTypeSerializer +
    StoryPackageArticleTypeSerializer +
    StoryPackageGroupSerializer +
    RemovePassthroughFieldsFromThriftStruct

  def destringifyFields: PartialFunction[JField, JField] = {
    case JField("summary", JString(s)) => JField("summary", JBool(s.toBoolean))
    case JField("keyEvent", JString(s)) => JField("keyEvent", JBool(s.toBoolean))
    case JField("pinned", JString(s)) => JField("pinned", JBool(s.toBoolean))
    case JField("showInRelatedContent", JString(s)) => JField("showInRelatedContent", JBool(s.toBoolean))
    case JField("shouldHideAdverts", JString(s)) => JField("shouldHideAdverts", JBool(s.toBoolean))
    case JField("hasStoryPackage", JString(s)) => JField("hasStoryPackage", JBool(s.toBoolean))
    case JField("isExpired", JString(s)) => JField("isExpired", JBool(s.toBoolean))
    case JField("syndicatable", JString(s)) => JField("syndicatable", JBool(s.toBoolean))
    case JField("subscriptionDatabases", JString(s)) => JField("subscriptionDatabases", JBool(s.toBoolean))
    case JField("developerCommunity", JString(s)) => JField("developerCommunity", JBool(s.toBoolean))
    case JField("commentable", JString(s)) => JField("commentable", JBool(s.toBoolean))
    case JField("liveBloggingNow", JString(s)) => JField("liveBloggingNow", JBool(s.toBoolean))
    case JField("isPremoderated", JString(s)) => JField("isPremoderated", JBool(s.toBoolean))
    case JField("wordcount", JString(s)) => JField("wordcount", JInt(s.toInt))
    case JField("newspaperPageNumber", JString(s)) => JField("newspaperPageNumber", JInt(s.toInt))
    case JField("starRating", JString(s)) => JField("starRating", JInt(s.toInt))
    case JField("isInappropriateForSponsorship", JString(s)) => JField("isInappropriateForSponsorship", JBool(s.toBoolean))
    case JField("isInappropriateForAdverts", JString(s)) => JField("isInappropriateForAdverts", JBool(s.toBoolean))
    case JField("internalPageCode", JString(s)) => JField("internalPageCode", JInt(s.toInt))
    case JField("internalStoryPackageCode", JString(s)) => JField("internalStoryPackageCode", JInt(s.toInt))
    case JField("width", JString(s)) => JField("width", JInt(s.toInt))
    case JField("height", JString(s)) => JField("height", JInt(s.toInt))
    case JField("duration", JString(s)) => JField("duration", JInt(s.toInt))
    case JField("durationMinutes", JString(s)) => JField("durationMinutes", JInt(s.toInt))
    case JField("durationSeconds", JString(s)) => JField("durationSeconds", JInt(s.toInt))
    case JField("isMaster", JString(s)) => JField("isMaster", JBool(s.toBoolean))
    case JField("sizeInBytes", JString(s)) => JField("sizeInBytes", JLong(s.toLong))
    case JField("blockAds", JString(s)) => JField("blockAds", JBool(s.toBoolean))
    case JField("allowUgc", JString(s)) => JField("allowUgc", JBool(s.toBoolean))
    case JField("displayCredit", JString(s)) => JField("displayCredit", JBool(s.toBoolean))
    case JField("legallySensitive", JString(s)) => JField("legallySensitive", JBool(s.toBoolean))
    case JField("sensitive", JString(s)) => JField("sensitive", JBool(s.toBoolean))
    case JField("explicit", JString(s)) => JField("explicit", JBool(s.toBoolean))
    case JField("clean", JString(s)) => JField("clean", JBool(s.toBoolean))
    case JField("safeEmbedCode", JString(s)) => JField("safeEmbedCode", JBool(s.toBoolean))
  }

  def thriftEnum2JString[A <: ThriftEnum](implicit classTag: ClassTag[A]): PartialFunction[Any, JString] = {
    case a if classTag.runtimeClass.isInstance(a) => JString(pascalCaseToHyphenated(a.asInstanceOf[A].name))
  }

  private def stringifyRecursively(json: JValue): JValue = json transformField {
    case (k, JInt(value)) => k -> JString(value.toString)
    case (k, JBool(value)) => k -> JString(value.toString)
  }

  /**
    * Stringify some parts of the JSON representation of a Content
    * for compatibility with existing CAPI JSON response format.
    */
  def stringifyContent(content: JValue): JValue = {
    val stringifyRights = (content: JValue) => content.replace(List("rights"), stringifyRecursively(content \ "rights"))
    val stringifyFields = (content: JValue) => content.replace(List("fields"), stringifyRecursively(content \ "fields"))
    val stringifyElements = (content: JValue) => content.replace(List("elements"), stringifyRecursively(content \ "elements"))

    (stringifyRights andThen stringifyFields andThen stringifyElements)(content)
  }

  /**
    * Normalise a string for use in enum lookup by removing hyphens.
    * Note that Thrift enum lookup using the `valueOf` method is not case-sensitive.
    */
  private def removeHyphens(s: String): String = s.replaceAllLiterally("-", "")

  private val LowerCaseFollowedByUpperCase = """([a-z])([A-Z])""".r

  /**
    * Convert a PascalCase string to a lowercase hyphenated string
    */
  private def pascalCaseToHyphenated(s: String): String =
    LowerCaseFollowedByUpperCase.replaceAllIn(s, m => m.group(1) + "-" + m.group(2)).toLowerCase

  object AtomHelper {
    def createChangeRecord(obj: JObject,  dateField: String, userField: String): Option[ChangeRecord] = {
      val optionalDate = (obj \ dateField).extractOpt[String].map(new DateTime(_).getMillis)
      optionalDate.map { d =>
        val user = (obj \ userField).extractOpt[com.gu.contentatom.thrift.User]
        ChangeRecord(d, user)
      }
    }

    def fixAtomsFields: PartialFunction[JField, JField] = {
      case JField("date", JString(s)) => JField("date", JLong(new DateTime(s).getMillis))
    }

    def createAtom(atom: JObject, atomType: AtomType, atomData: AtomData): Atom = {
      val atomId = (atom \ "id").extract[String]
      val labels = (atom \ "labels").extract[Seq[String]]
      val defaultHtml = (atom \ "defaultHtml").extract[String]

      val lastModified = createChangeRecord(atom, "lastModifiedDate", "lastModifiedBy")
      val created = createChangeRecord(atom, "createdDate", "createdBy")
      val published = createChangeRecord(atom, "publishDate", "publishBy")

      val revision = (atom \ "revision").extract[Long]
      val change = ContentChangeDetails(lastModified, created, published, revision)

      Atom(atomId, atomType, labels, defaultHtml, atomData, change)
    }

    // For adding a new atom type define a new function here and then add it to the AtomSerializer

    def getQuizIfExists(rawAtom: JObject): Option[Seq[Atom]] = {
      Try((rawAtom \ "quizzes").extract[Seq[JObject]]) match {
        case Success(quizAtoms) =>
          Some(quizAtoms map { quizAtom =>
            val atomData = AtomData.Quiz((quizAtom \ "data").extract[QuizAtom])
            createAtom(quizAtom, AtomType.Quiz, atomData)
          })
        case Failure(_) => None
      }
    }

    def getViewpointsIfExists(rawAtom: JObject): Option[Seq[Atom]] = {
      Try((rawAtom \ "viewpoints").extract[Seq[JObject]]) match {
        case Success(viewpointAtoms) =>
          Some(viewpointAtoms map { viewpointAtom =>
            val atomData = AtomData.Viewpoints((viewpointAtom \ "data").transformField(fixAtomsFields).extract[ViewpointsAtom])
            createAtom(viewpointAtom, AtomType.Viewpoints, atomData)
          })
        case Failure(_) => None
      }
    }
  }

  import AtomHelper._

  object AtomSerializer extends CustomSerializer[Atoms](format => (
    {
      case rawAtom: JObject => Atoms(quizzes = getQuizIfExists(rawAtom), viewpoints = getViewpointsIfExists(rawAtom))
    },
    thriftEnum2JString[ContentType] // FIXME needs to be implemented
    ))

  object ContentTypeSerializer extends CustomSerializer[ContentType](format => (
    {
      case JString(s) => ContentType.valueOf(removeHyphens(s)).getOrElse(ContentType.EnumUnknownContentType(-1))
    },
    thriftEnum2JString[ContentType]
    ))

  object MembershipTierSerializer extends CustomSerializer[MembershipTier](format => (
    {
      case JString(s) => MembershipTier.valueOf(removeHyphens(s)).getOrElse(MembershipTier.EnumUnknownMembershipTier(-1))
    },
    thriftEnum2JString[MembershipTier]
    ))

  object OfficeSerializer extends CustomSerializer[Office](format => (
    {
      case JString(s) => Office.valueOf(removeHyphens(s)).getOrElse(Office.EnumUnknownOffice(-1))
    },
    thriftEnum2JString[Office].andThen(jstring => JString(jstring.s.toUpperCase))
    ))

  object AssetTypeSerializer extends CustomSerializer[AssetType](format => (
    {
      case JString(s) => AssetType.valueOf(removeHyphens(s)).getOrElse(AssetType.EnumUnknownAssetType(-1))
    },
    thriftEnum2JString[AssetType]
    ))

  object ElementTypeSerializer extends CustomSerializer[ElementType](format => (
    {
      case JString(s) => ElementType.valueOf(removeHyphens(s)).getOrElse(ElementType.EnumUnknownElementType(-1))
    },
    thriftEnum2JString[ElementType]
    ))

  object SponsorshipTypeSerializer extends CustomSerializer[SponsorshipType](format => (
    {
      case JString(s) => SponsorshipType.valueOf(removeHyphens(s)).getOrElse(SponsorshipType.EnumUnknownSponsorshipType(-1))
    },
    thriftEnum2JString[SponsorshipType]
    ))

  object TagTypeSerializer extends CustomSerializer[TagType](format => (
    {
      case JString(s) => TagType.valueOf(removeHyphens(s)).getOrElse(TagType.EnumUnknownTagType(-1))
    },
    thriftEnum2JString[TagType]
    ))

  object CrosswordTypeSerializer extends CustomSerializer[CrosswordType](format => (
    {
      case JString(s) => CrosswordType.valueOf(removeHyphens(s)).getOrElse(CrosswordType.EnumUnknownCrosswordType(-1))
    },
    thriftEnum2JString[CrosswordType]
    ))

  object StoryPackageArticleTypeSerializer extends CustomSerializer[ArticleType](format => (
    {
      case JString(s) => ArticleType.valueOf(removeHyphens(s)).getOrElse(ArticleType.EnumUnknownArticleType(-1))
    },
    thriftEnum2JString[ArticleType]
    ))

  object StoryPackageGroupSerializer extends CustomSerializer[Group](format => (
    {
      case JString(s) => Group.valueOf(removeHyphens(s)).getOrElse(Group.EnumUnknownGroup(-1))
    },
    thriftEnum2JString[Group]
    ))

  object DateTimeSerializer extends CustomSerializer[CapiDateTime](format => (
    {
      case JString(s) => CapiDateTime.apply(ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(s).getMillis)
    },
    {
      case d: CapiDateTime => JString(new DateTime(d.dateTime).toString)
    }
    ))

  object RemovePassthroughFieldsFromThriftStruct extends FieldSerializer[ThriftStruct](serializer = FieldSerializer.ignore("_passthroughFields"))

}
