package net.gutefrage.finatra

import java.io.{ByteArrayOutputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import javax.inject.{Inject, Singleton}

import com.google.common.net.MediaType
import com.twitter.finatra.http.internal.marshalling.MessageBodyManager
import com.twitter.finatra.http.marshalling.{MessageBodyWriter, WriterResponse}
import com.twitter.inject.{Injector, InjectorModule, TwitterModule}
import com.twitter.io.{Buf, InputStreamReader}
import org.apache.commons.io.IOUtils

import scala.collection.JavaConverters._

/**
  * Copied code together from
  *
  * - MustacheTemplateNameLookup
  * - MustacheMessageBodyWriter
  * - MustacheService
  */
object HandlebarModules extends TwitterModule {

  override val modules = Seq(InjectorModule)

  override def singletonStartup(injector: Injector) {
    debug("Configuring MessageBodyManager for Handlebars")
    val manager = injector.instance[MessageBodyManager]
    manager.addByAnnotation[HandlebarsJava, HandlebarsJavaBodyWriter]()
    manager.addByAnnotation[HandelbarsScala, HandlebarsScalaBodyWriter]()
  }
}


@Singleton
class HandlebarsJavaBodyWriter @Inject()() extends MessageBodyWriter[Any] {
  import com.github.jknack.handlebars.Handlebars
  import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache
  import com.github.jknack.handlebars.io.ClassPathTemplateLoader

  // TODO use flag for template path
  private val handlebars = new Handlebars()
      .`with`(new ClassPathTemplateLoader("/templates"))
      .`with`(new ConcurrentMapTemplateCache)

  private val classToTemplateNameCache = new ConcurrentHashMap[Class[_], String]().asScala


  override def write(obj: Any): WriterResponse = {
    WriterResponse(
      MediaType.HTML_UTF_8,
      createBuffer(lookupViaAnnotation(obj), obj)
    )
  }

  // TODO validate vs normal template.apply(ctx)
  // TODO use a template cache
  private def createBuffer(templateName: String, obj: Any): Buf = {
    val template = handlebars.compile(templateName)

    val outputStream = new ByteArrayOutputStream(1024)
    val writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
    try {
      template(obj, writer)
    } finally {
      writer.close()
    }

    Buf.ByteArray.Owned(outputStream.toByteArray)
  }

  private def lookupViaAnnotation(viewObj: Any): String = {
    classToTemplateNameCache.getOrElseUpdate(viewObj.getClass, {
      val handlebarsJavaAnnotation = viewObj.getClass.getAnnotation(classOf[HandlebarsJava])
      handlebarsJavaAnnotation.value
    })
  }
}

@Singleton
class HandlebarsScalaBodyWriter @Inject()() extends MessageBodyWriter[Any] {
  import com.gilt.handlebars.scala.binding.dynamic._
  import com.gilt.handlebars.scala.Handlebars

  private val classToTemplateNameCache = new ConcurrentHashMap[Class[_], String]().asScala

  override def write(obj: Any): WriterResponse = {
    val location = lookupViaAnnotation(obj)
    val input = getClass.getResourceAsStream(s"/templates/$location")
    val template = Handlebars(IOUtils.toString(input, "UTF-8"))

    WriterResponse(
      MediaType.HTML_UTF_8,
      template.apply(obj)
    )
  }

  private def lookupViaAnnotation(viewObj: Any): String = {
    classToTemplateNameCache.getOrElseUpdate(viewObj.getClass, {
      val handlebarsScalaAnnotation = viewObj.getClass.getAnnotation(classOf[HandelbarsScala])
      handlebarsScalaAnnotation.value + ".hbs"
    })
  }
}