package pharmacie.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import pharmacie.service.ApprovisionnementService;

@Slf4j
@RestController
@RequestMapping("/api/approvisionnement")
public class ApprovisionnementController {

    private final ApprovisionnementService approvisionnementService;

    public ApprovisionnementController(ApprovisionnementService approvisionnementService) {
        this.approvisionnementService = approvisionnementService;
    }

    /**
     * Endpoint REST pour lancer le processus d'approvisionnement
     * Accepte GET et POST
     * 
     * @return Un message récapitulatif des actions effectuées
     */
    @RequestMapping(value = "/declencher", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> declencherApprovisionnement() {
        log.info("Requête pour déclencher l'approvisionnement reçue");
        String result = approvisionnementService.executerApprovisionnement();
        return ResponseEntity.ok(result);
    }

}
