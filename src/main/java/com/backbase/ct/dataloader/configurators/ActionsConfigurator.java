package com.backbase.ct.dataloader.configurators;

import static com.backbase.ct.dataloader.data.ActionsDataGenerator.generateActionRecipesPostRequestBody;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ACTIONS_MAX;
import static com.backbase.ct.dataloader.data.CommonConstants.PROPERTY_ACTIONS_MIN;
import static com.backbase.ct.dataloader.utils.CommonHelpers.generateRandomNumberInRange;
import static org.apache.http.HttpStatus.SC_ACCEPTED;

import com.backbase.ct.dataloader.clients.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.dataloader.clients.actions.ActionRecipesPresentationRestClient;
import com.backbase.ct.dataloader.clients.common.LoginRestClient;
import com.backbase.ct.dataloader.clients.productsummary.ProductSummaryPresentationRestClient;
import com.backbase.ct.dataloader.utils.GlobalProperties;
import com.backbase.dbs.actions.actionrecipes.presentation.rest.spec.v2.actionrecipes.ActionRecipesPostRequestBody;
import com.backbase.presentation.productsummary.rest.spec.v2.productsummary.ArrangementsByBusinessFunctionGetResponseBody;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserContextPresentationRestClient userContextPresentationRestClient = new UserContextPresentationRestClient();
    private ProductSummaryPresentationRestClient productSummaryPresentationRestClient = new ProductSummaryPresentationRestClient();
    private ActionRecipesPresentationRestClient actionRecipesPresentationRestClient = new ActionRecipesPresentationRestClient();
    private Random random = new Random();

    public void ingestActions(String externalUserId) {
        List<ArrangementsByBusinessFunctionGetResponseBody> arrangements = new ArrayList<>();
        int randomAmount = generateRandomNumberInRange(globalProperties.getInt(PROPERTY_ACTIONS_MIN),
                globalProperties.getInt(PROPERTY_ACTIONS_MAX));

        loginRestClient.login(externalUserId, externalUserId);
        userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();

        arrangements.addAll(productSummaryPresentationRestClient.getSepaCtArrangements());
        arrangements.addAll(productSummaryPresentationRestClient.getUsDomesticWireArrangements());

        IntStream.range(0, randomAmount).parallel().forEach(randomNumber -> {
            String internalArrangementId = arrangements.get(random.nextInt(arrangements.size())).getId();

            ActionRecipesPostRequestBody actionRecipesPostRequestBody = generateActionRecipesPostRequestBody(
                internalArrangementId);

            actionRecipesPresentationRestClient.createActionRecipe(actionRecipesPostRequestBody)
                .then()
                .statusCode(SC_ACCEPTED);

            LOGGER.info("Action ingested with specification id [{}] for arrangement [{}]",
                actionRecipesPostRequestBody.getSpecificationId(), actionRecipesPostRequestBody.getArrangementId());
        });
    }
}