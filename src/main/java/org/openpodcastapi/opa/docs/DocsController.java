package org.openpodcastapi.opa.docs;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocsController {

    @GetMapping("/docs")
    public String docs() {
        return "forward:/docs/index.html";
    }

    @GetMapping("/docs/")
    public String docsWithSlash() {
        return "forward:/docs/index.html";
    }
}