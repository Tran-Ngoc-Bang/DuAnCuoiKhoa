package com.fpoly.shared_learning_materials.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for client document-related pages
 */
@Controller
@RequestMapping("/documents")
public class ClientDocumentController {

    @GetMapping("/{id}")
    public String documentDetails(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Chi tiết tài liệu");
        model.addAttribute("documentId", id);
        return "client/document-details";
    }

    @GetMapping("/{id}/view")
    public String viewDocument(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Xem tài liệu");
        model.addAttribute("documentId", id);
        return "client/pdf-viewer";
    }

    @GetMapping("/{id}/download")
    public String downloadDocument(@PathVariable Long id) {
        // Logic for document download will be implemented later
        return "redirect:/documents/" + id;
    }
}