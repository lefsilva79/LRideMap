package com.lefsilva.lridemap

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lefsilva.lridemap.ui.theme.LRideMapTheme

@Composable
fun MainScreen(
    isAccessibilityEnabled: Boolean,
    isOverlayEnabled: Boolean,
    isLocationEnabled: Boolean,
    onAccessibilityClick: () -> Unit,
    onOverlayClick: () -> Unit,
    onLocationClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bem-vindo ao LRideMap",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Permissão de Acessibilidade
            PermissionItem(
                title = "Serviço de Acessibilidade",
                description = "Necessário para detectar endereços no Lyft",
                isEnabled = isAccessibilityEnabled,
                onClick = onAccessibilityClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Permissão de Overlay
            PermissionItem(
                title = "Permissão de Sobreposição",
                description = "Necessário para mostrar o botão flutuante",
                isEnabled = isOverlayEnabled,
                onClick = onOverlayClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Permissão de Localização
            PermissionItem(
                title = "Permissão de Localização",
                description = "Necessário para mostrar sua posição no mapa",
                isEnabled = isLocationEnabled,
                onClick = onLocationClick
            )
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (isEnabled) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Permitido",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )

            if (!isEnabled) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Permitir")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    LRideMapTheme {
        MainScreen(
            isAccessibilityEnabled = true,
            isOverlayEnabled = false,
            isLocationEnabled = false,
            onAccessibilityClick = {},
            onOverlayClick = {},
            onLocationClick = {}
        )
    }
}