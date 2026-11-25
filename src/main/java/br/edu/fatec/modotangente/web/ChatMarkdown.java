package br.edu.fatec.modotangente.web;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.CoreHtmlNodeRenderer;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
class ChatMarkdown {

    private static final List<Extension> EXTENSIONS = List.of(TablesExtension.create());

    private final Parser parser = Parser.builder().extensions(EXTENSIONS).build();
    private final HtmlRenderer renderer = HtmlRenderer.builder()
            .extensions(EXTENSIONS)
            .escapeHtml(true)
            .sanitizeUrls(true)
            .softbreak("<br>\n")
            .nodeRendererFactory(context -> new CoreHtmlNodeRenderer(context) {
                @Override
                public Set<Class<? extends Node>> getNodeTypes() {
                    return Set.of(Image.class);
                }

                @Override
                public void visit(Image image) {
                    // Show the description without loading an external image.
                    visitChildren(image);
                }
            })
            .build();

    String render(String content) {
        return renderer.render(parser.parse(content));
    }
}
