import re

with open('app/src/main/java/com/example/ui/screens/auth/AuthScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''            // Google Sign In Button
            Button(
                onClick = {''',
'''            // Guest Login Button
            Button(
                onClick = {
                    viewModel.signInAnonymously()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGrey),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "المتابعة كضيف",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign In Button
            Button(
                onClick = {'''
)

with open('app/src/main/java/com/example/ui/screens/auth/AuthScreen.kt', 'w') as f:
    f.write(content)
