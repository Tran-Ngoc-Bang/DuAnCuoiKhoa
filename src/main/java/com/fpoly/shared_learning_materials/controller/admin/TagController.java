package com.fpoly.shared_learning_materials.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fpoly.shared_learning_materials.service.TagService;

import jakarta.validation.Valid;

import com.fpoly.shared_learning_materials.domain.Tag;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.dto.TagDTO;
import com.fpoly.shared_learning_materials.repository.UserRepository;

import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping("/admin/tags")
public class TagController {

	 @Autowired
	    private TagService tagService;
	 
	 @Autowired
	 private UserRepository userRepository;

	 @GetMapping
	    public String listTags(
	            @RequestParam(defaultValue = "0") int page,
	            @RequestParam(defaultValue = "10") int size,
	            @RequestParam(defaultValue = "month") String filter,
	            @RequestParam(defaultValue = "recent") String sort,
	            Model model) {
	        Page<TagDTO> tags = tagService.getAllTags(page, size, sort);
	        List<TagDTO> filteredTags = tagService.getFilteredTags(filter);
//	        System.out.println("Filter applied for tag-cloud: " + filter + ", Filtered tags count: " + filteredTags.size());
//	        System.out.println("Sort applied for table: " + sort + ", Tags count: " + tags.getContent().size());
//	        System.out.println("Total items: " + tags.getTotalElements());
//	        System.out.println("Total tags: " + tagService.countAllTags());
//	        System.out.println("Documents with tags: " + tagService.countDocumentsWithTags());
//	        System.out.println("New tags this month: " + tagService.countTagsCreatedThisMonth());

	        model.addAttribute("tags", tags);
	        model.addAttribute("filteredTags", filteredTags);
	        model.addAttribute("currentPage", page);
	        model.addAttribute("pageSize", size);
	        model.addAttribute("totalPages", tags.getTotalPages());
	        model.addAttribute("totalItems", tags.getTotalElements());
	        model.addAttribute("documentsWithTags", tagService.countDocumentsWithTags());
	        model.addAttribute("newTagsThisMonth", tagService.countTagsCreatedThisMonth());
	        model.addAttribute("filter", filter);
	        model.addAttribute("sort", sort);

	        return "admin/tag/list";
	    }
	 


	 @GetMapping("/create")
	    public String showCreateForm(Model model) {
	        if (!model.containsAttribute("tagDTO")) {
	            TagDTO tagDTO = new TagDTO();
	            tagDTO.setColor("#4361ee"); // Mặc định màu
	            tagDTO.setStatus("active"); // Mặc định trạng thái
	            model.addAttribute("tagDTO", tagDTO);
	        }
	        return "admin/tag/create";
	    }
	 
	 
	 @PostMapping("/create")
	    public String createTag(@Valid @ModelAttribute("tagDTO") TagDTO tagDTO, BindingResult result, Model model) {

		 Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        Long userId = null;

	        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
	            Object principal = authentication.getPrincipal();
	            if (principal instanceof UserDetails) {
	                String username = ((UserDetails) principal).getUsername();
	                User user = userRepository.findByUsername(username)
	                        .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng với username: " + username));
	                userId = user.getId();
	            } else {
	                model.addAttribute("errorMessage", "Không thể xác định thông tin người dùng đã đăng nhập");
	                return "admin/tags/create";
	            }
	        } else {
	            model.addAttribute("errorMessage", "Bạn cần đăng nhập để tạo tag");
	            return "admin/tags/create";
	        }

	        tagDTO.setCreatedById(userId);
		 
	    
//		 	tagDTO.setCreatedById(3L);
	        if (result.hasErrors()) {
	            return "admin/tags/create";
	        }
	        try {
	            tagService.createTag(tagDTO);

	        } catch (Exception e) {

	            model.addAttribute("errorMessage", "Lỗi khi tạo tag: " + e.getMessage());
	            return "admin/tag/create";
	        }
	        return "redirect:/admin/tags?filter=month";
	    }
	    
	    
	    

	 @GetMapping("/{id}/edit")
	 public String showEditForm(@PathVariable Long id, Model model) {
	     TagDTO tagDTO = tagService.getTagById(id)
	             .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));
	     model.addAttribute("tagDTO", tagDTO);
	     return "admin/tag/edit";
	 }
	 
	 @PostMapping("/{id}/edit")
	 public String editTag(@PathVariable Long id, @Valid @ModelAttribute("tagDTO") TagDTO tagDTO, BindingResult result, Model model) {
	     if (result.hasErrors()) {
	         return "admin/tag/edit";
	     }
	     try {
	         tagDTO.setId(id); 
	         tagService.updateTag(tagDTO);
	         return "redirect:/admin/tags?filter=month";
	     } catch (IllegalArgumentException e) {
	         model.addAttribute("errorMessage", e.getMessage());
	         return "admin/tag/edit";
	     } catch (Exception e) {
	         model.addAttribute("errorMessage", "Lỗi khi cập nhật tag: " + e.getMessage());
	         return "admin/tag/edit";
	     }
	 }
	    
	    

	 @GetMapping("/{id}/details")
	 public String showTagDetails(@PathVariable Long id, Model model) {
	     TagDTO tag = tagService.getTagById(id)
	             .orElseThrow(() -> new RuntimeException("Tag not found"));
	     model.addAttribute("tag", tag); 
	     return "admin/tag/details";
	 }

	    @PostMapping("/{id}/delete")
	    public String deleteTag(@PathVariable Long id) {
	        tagService.deleteTag(id);
	        return "redirect:/admin/tags";
	    }
	}