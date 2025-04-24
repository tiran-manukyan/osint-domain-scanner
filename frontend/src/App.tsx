import HomePage from "./pages/HomePage"
import { CssBaseline, ThemeProvider, createTheme } from "@mui/material"
import "./styles/global.css"

const theme = createTheme({
    palette: {
        primary: {
            main: "#3a86ff",
            light: "#6ea8ff",
            dark: "#2667cc",
        },
        secondary: {
            main: "#4361ee",
        },
        background: {
            default: "#f8fafc",
            paper: "#ffffff",
        },
        text: {
            primary: "#1f2937",
            secondary: "#6b7280",
        },
    },
    typography: {
        fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    },
    shape: {
        borderRadius: 8,
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    textTransform: "none",
                    fontWeight: 600,
                },
            },
        },
    },
})

export default function App() {
    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <HomePage />
        </ThemeProvider>
    )
}
