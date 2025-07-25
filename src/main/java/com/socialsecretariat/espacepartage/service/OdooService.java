package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.config.OdooConfig;
import com.socialsecretariat.espacepartage.exception.OdooIntegrationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OdooService {

    private final OdooConfig odooConfig;
    private XmlRpcClient commonClient;
    private XmlRpcClient modelsClient;
    private Integer uid;

    /**
     * Authentifie l'utilisateur auprès d'Odoo et initialise les clients XML-RPC
     */
    private void authenticateToOdoo() {
        try {
            if (commonClient == null) {
                // Configuration du client common
                XmlRpcClientConfigImpl commonConfig = new XmlRpcClientConfigImpl();
                commonConfig.setServerURL(new URL(odooConfig.getUrl() + "/xmlrpc/2/common"));
                commonClient = new XmlRpcClient();
                commonClient.setConfig(commonConfig);

                // Configuration du client models
                XmlRpcClientConfigImpl modelsConfig = new XmlRpcClientConfigImpl();
                modelsConfig.setServerURL(new URL(odooConfig.getUrl() + "/xmlrpc/2/object"));
                modelsClient = new XmlRpcClient();
                modelsClient.setConfig(modelsConfig);
            }

            if (uid == null) {
                // Authentification
                uid = (Integer) commonClient.execute("authenticate", Arrays.asList(
                    odooConfig.getDatabase(),
                    odooConfig.getUsername(),
                    odooConfig.getPassword(),
                    new HashMap<>()
                ));

                if (uid == null || uid == 0) {
                    throw new OdooIntegrationException("Échec de l'authentification Odoo");
                }

                log.info("Authentification Odoo réussie avec UID: {}", uid);
            }

        } catch (MalformedURLException e) {
            throw new OdooIntegrationException("URL Odoo invalide: " + odooConfig.getUrl(), e);
        } catch (XmlRpcException e) {
            throw new OdooIntegrationException("Erreur lors de l'authentification Odoo", e);
        }
    }

    /**
     * Recherche ou crée un partenaire dans Odoo
     * 
     * @param companyRegistry Numéro d'entreprise (BCE)
     * @param companyName Nom de l'entreprise
     * @param email Email de l'entreprise
     * @param phone Téléphone de l'entreprise
     * @return ID du partenaire dans Odoo
     */
    public Long findOrCreatePartner(String companyRegistry, String companyName, String email, String phone) {
        try {
            authenticateToOdoo();

            // Recherche du partenaire existant
            Object[] searchResult = (Object[]) modelsClient.execute("execute_kw", Arrays.asList(
                odooConfig.getDatabase(),
                uid,
                odooConfig.getPassword(),
                "res.partner",
                "search_read",
                Arrays.asList(Arrays.asList(
                    Arrays.asList("company_registry", "=", companyRegistry),
                    Arrays.asList("company_type", "=", "company")
                )),
                Map.of("fields", Arrays.asList("id", "name"), "limit", 1)
            ));

            if (searchResult.length > 0) {
                // Partenaire trouvé
                Map<String, Object> partner = (Map<String, Object>) searchResult[0];
                Integer partnerId = (Integer) partner.get("id");
                log.info("Partenaire trouvé : ID = {}", partnerId);
                return partnerId.longValue();
            } else {
                // Créer un nouveau partenaire
                Map<String, Object> partnerData = new HashMap<>();
                partnerData.put("name", companyName);
                partnerData.put("email", email);
                partnerData.put("phone", phone);
                partnerData.put("company_registry", companyRegistry);
                partnerData.put("company_type", "company");
                partnerData.put("is_company", true);

                Integer partnerId = (Integer) modelsClient.execute("execute_kw", Arrays.asList(
                    odooConfig.getDatabase(),
                    uid,
                    odooConfig.getPassword(),
                    "res.partner",
                    "create",
                    Arrays.asList(partnerData)
                ));

                log.info("Nouveau partenaire créé : ID = {}", partnerId);
                return partnerId.longValue();
            }

        } catch (XmlRpcException e) {
            throw new OdooIntegrationException("Erreur lors de la recherche/création du partenaire", e);
        }
    }

    /**
     * Crée une tâche dans Odoo
     * 
     * @param taskName Nom de la tâche
     * @param description Description de la tâche
     * @param deadline Date limite (format YYYY-MM-DD)
     * @param priority Priorité (0=Low, 1=Normal, 2=High, 3=Very High)
     * @param partnerId ID du partenaire (peut être null)
     * @param projectId ID du projet
     * @return ID de la tâche créée
     */
    public Long createTask(String taskName, String description, String deadline, String priority, Long partnerId, Long projectId) {
        try {
            authenticateToOdoo();

            Map<String, Object> taskData = new HashMap<>();
            taskData.put("name", taskName);
            taskData.put("description", description);
            taskData.put("date_deadline", deadline);
            taskData.put("priority", priority);
            taskData.put("project_id", projectId.intValue());
            
            if (partnerId != null) {
                taskData.put("partner_id", partnerId.intValue());
            }

            Integer taskId = (Integer) modelsClient.execute("execute_kw", Arrays.asList(
                odooConfig.getDatabase(),
                uid,
                odooConfig.getPassword(),
                "project.task",
                "create",
                Arrays.asList(taskData)
            ));

            log.info("Tâche créée avec succès : ID = {}", taskId);
            return taskId.longValue();

        } catch (XmlRpcException e) {
            throw new OdooIntegrationException("Erreur lors de la création de la tâche", e);
        }
    }
}
