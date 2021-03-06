package com.twilio.guardrail.protocol.terms.server

import cats.Monad
import com.twilio.guardrail.core.Tracker
import com.twilio.guardrail.generators.LanguageParameters
import com.twilio.guardrail.languages.LA
import com.twilio.guardrail.protocol.terms.Responses
import com.twilio.guardrail.terms.{ RouteMeta, SecurityScheme }
import com.twilio.guardrail.{ RenderedRoutes, StrictProtocolElems, SupportDefinition, TracingField }
import io.swagger.v3.oas.models.Operation

abstract class ServerTerms[L <: LA, F[_]] {
  def MonadF: Monad[F]
  def buildTracingFields(operation: Tracker[Operation], resourceName: List[String], tracing: Boolean): F[Option[TracingField[L]]]
  def generateRoutes(
      tracing: Boolean,
      resourceName: String,
      basePath: Option[String],
      routes: List[(String, Option[TracingField[L]], RouteMeta, LanguageParameters[L], Responses[L])],
      protocolElems: List[StrictProtocolElems[L]],
      securitySchemes: Map[String, SecurityScheme[L]]
  ): F[RenderedRoutes[L]]
  def getExtraRouteParams(tracing: Boolean): F[List[L#MethodParameter]]
  def generateResponseDefinitions(operationId: String, responses: Responses[L], protocolElems: List[StrictProtocolElems[L]]): F[List[L#Definition]]
  def generateSupportDefinitions(tracing: Boolean, securitySchemes: Map[String, SecurityScheme[L]]): F[List[SupportDefinition[L]]]
  def renderClass(
      resourceName: String,
      handlerName: String,
      annotations: List[L#Annotation],
      combinedRouteTerms: List[L#Term],
      extraRouteParams: List[L#MethodParameter],
      responseDefinitions: List[L#Definition],
      supportDefinitions: List[L#Definition]
  ): F[List[L#Definition]]
  def renderHandler(
      handlerName: String,
      methodSigs: List[L#MethodDeclaration],
      handlerDefinitions: List[L#Statement],
      responseDefinitions: List[L#Definition]
  ): F[L#Definition]
  def getExtraImports(tracing: Boolean): F[List[L#Import]]

  def copy(
      newMonadF: Monad[F] = MonadF,
      newBuildTracingFields: (Tracker[Operation], List[String], Boolean) => F[Option[TracingField[L]]] = buildTracingFields _,
      newGenerateRoutes: (
          Boolean,
          String,
          Option[String],
          List[(String, Option[TracingField[L]], RouteMeta, LanguageParameters[L], Responses[L])],
          List[StrictProtocolElems[L]],
          Map[String, SecurityScheme[L]]
      ) => F[RenderedRoutes[L]] = generateRoutes _,
      newGetExtraRouteParams: Boolean => F[List[L#MethodParameter]] = getExtraRouteParams _,
      newGenerateResponseDefinitions: (String, Responses[L], List[StrictProtocolElems[L]]) => F[List[L#Definition]] = generateResponseDefinitions _,
      newGenerateSupportDefinitions: (Boolean, Map[String, SecurityScheme[L]]) => F[List[SupportDefinition[L]]] = generateSupportDefinitions _,
      newRenderClass: (
          String,
          String,
          List[L#Annotation],
          List[L#Term],
          List[L#MethodParameter],
          List[L#Definition],
          List[L#Definition]
      ) => F[List[L#Definition]] = renderClass _,
      newRenderHandler: (String, List[L#MethodDeclaration], List[L#Statement], List[L#Definition]) => F[L#Definition] = renderHandler _,
      newGetExtraImports: Boolean => F[List[L#Import]] = getExtraImports _
  ) = new ServerTerms[L, F] {
    def MonadF = newMonadF
    def buildTracingFields(operation: Tracker[Operation], resourceName: List[String], tracing: Boolean) =
      newBuildTracingFields(operation, resourceName, tracing)
    def generateRoutes(
        tracing: Boolean,
        resourceName: String,
        basePath: Option[String],
        routes: List[(String, Option[TracingField[L]], RouteMeta, LanguageParameters[L], Responses[L])],
        protocolElems: List[StrictProtocolElems[L]],
        securitySchemes: Map[String, SecurityScheme[L]]
    )                                         = newGenerateRoutes(tracing, resourceName, basePath, routes, protocolElems, securitySchemes)
    def getExtraRouteParams(tracing: Boolean) = newGetExtraRouteParams(tracing)
    def generateResponseDefinitions(operationId: String, responses: Responses[L], protocolElems: List[StrictProtocolElems[L]]) =
      newGenerateResponseDefinitions(operationId, responses, protocolElems)
    def generateSupportDefinitions(tracing: Boolean, securitySchemes: Map[String, SecurityScheme[L]]) = newGenerateSupportDefinitions(tracing, securitySchemes)
    def renderClass(
        resourceName: String,
        handlerName: String,
        annotations: List[L#Annotation],
        combinedRouteTerms: List[L#Term],
        extraRouteParams: List[L#MethodParameter],
        responseDefinitions: List[L#Definition],
        supportDefinitions: List[L#Definition]
    ) = newRenderClass(resourceName, handlerName, annotations, combinedRouteTerms, extraRouteParams, responseDefinitions, supportDefinitions)
    def renderHandler(
        handlerName: String,
        methodSigs: List[L#MethodDeclaration],
        handlerDefinitions: List[L#Statement],
        responseDefinitions: List[L#Definition]
    )                                     = newRenderHandler(handlerName, methodSigs, handlerDefinitions, responseDefinitions)
    def getExtraImports(tracing: Boolean) = newGetExtraImports(tracing)
  }
}
