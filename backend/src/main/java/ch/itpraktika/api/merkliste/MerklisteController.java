package ch.itpraktika.api.merkliste;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/merkliste")
@PreAuthorize("hasRole('STUDENT')")
public class MerklisteController {

    private final MerklisteService merklisteService;

    public MerklisteController(MerklisteService merklisteService) {
        this.merklisteService = merklisteService;
    }

    @GetMapping
    public List<MerklisteResponse> getMerkliste(Authentication authentication) {
        return merklisteService.getMerkliste(authentication.getName());
    }

    @GetMapping("/{eintragId}")
    public MerklisteResponse getEintrag(
            @PathVariable UUID eintragId,
            Authentication authentication
    ) {
        return merklisteService.getEintrag(authentication.getName(), eintragId);
    }

    @PostMapping
    public ResponseEntity<MerklisteResponse> addEintrag(
            @Valid @RequestBody MerklisteRequest request,
            Authentication authentication
    ) {
        MerklisteResponse response = merklisteService.addEintrag(authentication.getName(), request);
        return ResponseEntity
                .created(URI.create("/api/v1/me/merkliste/" + response.id()))
                .body(response);
    }

    @PutMapping("/{eintragId}")
    public MerklisteResponse updateEintrag(
            @PathVariable UUID eintragId,
            @Valid @RequestBody MerklisteRequest request,
            Authentication authentication
    ) {
        return merklisteService.updateEintrag(authentication.getName(), eintragId, request);
    }

    @DeleteMapping("/{eintragId}")
    public ResponseEntity<Void> deleteEintrag(
            @PathVariable UUID eintragId,
            Authentication authentication
    ) {
        merklisteService.deleteEintrag(authentication.getName(), eintragId);
        return ResponseEntity.noContent().build();
    }
}
