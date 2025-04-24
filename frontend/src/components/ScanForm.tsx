import type React from "react"
import {useState} from "react"
import {Box, Button, IconButton, InputAdornment, Paper, TextField, Tooltip, Typography} from "@mui/material"
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined"
import SearchIcon from "@mui/icons-material/Search"
import TimerOutlinedIcon from "@mui/icons-material/TimerOutlined"
import {startScan} from "../services/scanService"
import type {ScanRequest} from "../types/scan"
import "../styles/ScanForm.css"

const isValidDomain = (value: string): boolean => {
    try {
        const url = new URL(value.includes("://") ? value : `https://${value}`)
        const host = url.hostname
        return /^[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(host)
    } catch {
        return false
    }
}

interface Props {
    onScanComplete: (message: string) => void
    onScanError: (error: string) => void
}

const ScanForm: React.FC<Props> = ({onScanComplete, onScanError}) => {
    const [domain, setDomain] = useState("")
    const [timeoutMinutes, setTimeoutMinutes] = useState("0")

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()

        const rawDomain = domain.trim().replace(/\/+$/, "")
        if (!rawDomain) {
            onScanError("Domain is required.")
            return
        }

        if (!isValidDomain(rawDomain)) {
            onScanError("Invalid domain format.")
            return
        }

        const timeout = timeoutMinutes.trim() ? Number.parseInt(timeoutMinutes.trim(), 10) : undefined

        const req: ScanRequest = {
            domain: rawDomain,
            timeoutMinutes: timeout,
        }

        try {
            const response = await startScan(req)
            const message = response.message || "Scan started successfully."
            onScanComplete(message)
            setDomain("")
            setTimeoutMinutes("0")
        } catch (error: unknown) {
            let errMsg = "Failed to start scan."
            if (typeof error === "object" && error !== null && "response" in error) {
                const err = error as { response?: { data?: { message?: string } } }
                errMsg = err.response?.data?.message || errMsg
            }
            onScanError(errMsg)
        }
    }

    return (
        <Paper className="scan-form">
            <Typography variant="h5" className="scan-form-title">
                Start a New Scan
            </Typography>

            <Box component="form" onSubmit={handleSubmit} className="scan-form-container">
                <TextField
                    label="Domain"
                    value={domain}
                    onChange={(e) => setDomain(e.target.value)}
                    required
                    placeholder="example.com"
                    className="domain-field"
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                <SearchIcon color="primary"/>
                            </InputAdornment>
                        ),
                    }}
                />

                <Tooltip
                    title="Specify timeout in minutes. The scan will be terminated if it runs longer than the timeout.
                     Set to 0 to disable the timeout and let the scan run until it finishes naturally."
                    arrow
                >
                    <TextField
                        label="Timeout (min)"
                        type="number"
                        value={timeoutMinutes}
                        onChange={(e) => setTimeoutMinutes(e.target.value)}
                        className="timeout-field"
                        InputProps={{
                            startAdornment: (
                                <InputAdornment position="start">
                                    <TimerOutlinedIcon color="primary"/>
                                </InputAdornment>
                            ),
                            endAdornment: (
                                <InputAdornment position="end">
                                    <IconButton edge="end" tabIndex={-1} size="small">
                                        <InfoOutlinedIcon fontSize="small"/>
                                    </IconButton>
                                </InputAdornment>
                            ),
                            inputProps: {min: 0},
                        }}
                    />
                </Tooltip>

                <Button type="submit" variant="contained" className="scan-button">
                    Start Scan
                </Button>
            </Box>
        </Paper>
    )
}

export default ScanForm
