package com.drafthouse.mcc.core

import com.twitter.finagle.http.Request
import com.typesafe.config.Config
import javax.inject.Inject

/**
  * Created by A.J. Whitney <ajwhitney@drafthouse.com> on 2/4/20.
  */
class GlobalRedirectController @Inject()
(
  config: Config
) extends AdcBaseController {

  def docsRedirect(req: Request) = response.temporaryRedirect.header("Location", s"${servicePrefix}/api-docs/ui")

  get(s"${servicePrefix}/")(docsRedirect)
  get(s"${servicePrefix}/doc")(docsRedirect)
  get(s"${servicePrefix}/docs")(docsRedirect)

}
