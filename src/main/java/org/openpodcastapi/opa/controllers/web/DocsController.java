package org.openpodcastapi.opa.controllers.web;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/// Controller for the hosted documentation endpoints
@Controller
@Log4j2
public class DocsController {

    /// The hosted documentation endpoint. Redirects users to the index page.
    ///
    /// @return a redirect to the documentation
    @GetMapping("/docs")
    public String docs() {
        return "forward:/docs/index.html";
    }

    /// The hosted documentation endpoint (with a trailing slash).
    /// Redirects users to the index page.
    ///
    /// @return a redirect to the documentation
    @GetMapping("/docs/")
    public String docsWithSlash() {
        return "forward:/docs/index.html";
    }
}