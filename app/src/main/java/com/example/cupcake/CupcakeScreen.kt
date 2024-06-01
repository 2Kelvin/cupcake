package com.example.cupcake

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cupcake.data.DataSource
import com.example.cupcake.ui.OrderSummaryScreen
import com.example.cupcake.ui.OrderViewModel
import com.example.cupcake.ui.SelectOptionScreen
import com.example.cupcake.ui.StartOrderScreen

enum class CupcakeScreen {
    Start,
    Flavor,
    Pickup,
    Summary
}

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CupcakeAppBar(
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Composable
fun CupcakeApp(
    viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        topBar = {
            CupcakeAppBar(
                canNavigateBack = false,
                navigateUp = { /* TODO: implement back navigation */ }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        // navHost composable displays other composable (destinations) depending on the route
        NavHost(
            navController = navController, // used to navigate between screens using the navigate() method
            startDestination = CupcakeScreen.Start.name, // first screen of the app | default destination
            modifier = Modifier.padding(innerPadding)
        ) {
            // composable takes in 2 parameters
            // 'route' and the trailing lambda function 'content' (composable for the route)
            // displaying 'StartOrderScreen' for the route 'CupcakeScreen.Start.name'
            composable(route = CupcakeScreen.Start.name) {
                StartOrderScreen(
                    quantityOptions = DataSource.quantityOptions,
                    onNextButtonClicked = {
                        // carrying the right amount of cupcakes picked
                        viewModel.setQuantity(it)
                        // navigating to the next screen => flavor screen
                        navController.navigate(CupcakeScreen.Flavor.name)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            }

            // displaying 'SelectOptionScreen' for the route 'CupcakeScreen.Flavor.name'
            composable(route = CupcakeScreen.Flavor.name) {
                // context will allow to access string resource ids defined in the app resources
                // to get the list of flavors from the viewModel as string resources
                val context = LocalContext.current

                SelectOptionScreen(
                    subtotal = uiState.price,
                    options = DataSource.flavors.map { id ->
                        context.resources.getString(id)
                    },
                    onSelectionChanged ={ viewModel.setFlavor(it) },
                    // navigating to the Pick date screen
                    onNextButtonClicked = { navController.navigate(CupcakeScreen.Pickup.name) },
                    onCancelButtonClicked = { cancelOrderAndNavigateToStart(viewModel, navController) },
                    modifier = Modifier.fillMaxHeight()
                )
            }

            // displaying 'SelectOptionScreen' for the route 'CupcakeScreen.Pickup.name'
            composable(route = CupcakeScreen.Pickup.name) {
                SelectOptionScreen(
                    subtotal = uiState.price,
                    options = uiState.pickupOptions,
                    onSelectionChanged ={ viewModel.setDate(it) },
                    // navigating to the summary screen
                    onNextButtonClicked = { navController.navigate(CupcakeScreen.Summary.name) },
                    onCancelButtonClicked = { cancelOrderAndNavigateToStart(viewModel, navController) },
                    modifier = Modifier.fillMaxHeight()
                )
            }

            // displaying 'OrderSummaryScreen' for the route 'CupcakeScreen.Summary.name'
            composable(route = CupcakeScreen.Summary.name) {
                OrderSummaryScreen(
                    orderUiState = uiState,
                    onSendButtonClicked = { subject: String, summary: String ->},
                    onCancelButtonClicked = { cancelOrderAndNavigateToStart(viewModel, navController) },
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

// cancel buttons logic
// popping all back stack screens & navigating back to the start screen
// also resets the viewmodel state to default values
private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    // resetting all the state values
    viewModel.resetOrder()
    navController.popBackStack(
        route = CupcakeScreen.Start.name, // navigate back to start screen
        inclusive = false // don't pop the start screen
    )
}