import React from 'react';
import { Box, TextField, Button, Typography, FormControl, InputLabel, Select, MenuItem } from '@mui/material';

const SiteForm: React.FC = () => {
    return (
        <Box sx={{ padding: 2, width:360 }}>
            <Typography variant="h6">Add / Edit Site</Typography>
            <TextField label="Title" fullWidth sx={{ marginBottom: 2 }} />
            <TextField label="URL" fullWidth sx={{ marginBottom: 2 }} />
            <Button variant="contained" sx={{ marginBottom: 2 }}>Find Sitemaps</Button>
            <TextField
                label="Enter Sitemap-URLs manually one per line"
                multiline
                rows={4}
                fullWidth
                sx={{ marginBottom: 2 }}
            />
            <Button variant="contained" sx={{ marginBottom: 2 }}>Check Sitemaps</Button>
            <FormControl fullWidth sx={{ marginBottom: 2 }}>
                <InputLabel>Auto Crawl</InputLabel>
                <Select defaultValue="disabled">
                    <MenuItem value="disabled">Disabled</MenuItem>
                    <MenuItem value="daily">Daily</MenuItem>
                    <MenuItem value="weekly">Weekly</MenuItem>
                    <MenuItem value="monthly">Monthly</MenuItem>
                </Select>
            </FormControl>
            <TextField label="Receive Results" fullWidth sx={{ marginBottom: 2 }} />
            <Button variant="contained" color="error" sx={{ marginRight: 2 }}>Delete</Button>
            <Button variant="contained" color="secondary" sx={{ marginRight: 2 }}>Abort</Button>
            <Button variant="contained" color="primary">Add/Save</Button>
        </Box>
    );
};

export default SiteForm;
