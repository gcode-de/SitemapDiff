import React from 'react';
import {Box, Checkbox, IconButton, List, ListItem, ListItemText, Typography} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';

import {Crawl} from "../types/Crawl.tsx";

type SiteItemProps = {
    crawl: Crawl;
    baseURL: string;
    handleCheckUrl: (crawlId: string, url: string) => void;
}

const CrawlItem: React.FC<SiteItemProps> = ({crawl, baseURL, handleCheckUrl}: SiteItemProps) => {

    function truncateTextFromStart(text: string, maxLength: number) {
        if (text.length > maxLength) {
            return '...' + text.slice(-maxLength);
        }
        return text;
    }

    const {diffToPrevCrawl} = crawl;
    const diffLengthLimit = 25;
    const diffToPrevCrawlWithLimit = diffToPrevCrawl.slice(0, diffLengthLimit);

    return (
        <List sx={{padding: 0}}>
            {diffToPrevCrawlWithLimit.map((diff) => (
                <ListItem
                    key={diff.url}
                    secondaryAction={
                        <IconButton edge="end" aria-label="mark as done" sx={{padding: '0px', minHeight: '24px'}}>
                            <Checkbox
                                checked={diff.checked || false}
                                onChange={() => handleCheckUrl(crawl.id, diff.url)}
                                inputProps={{'aria-label': 'controlled'}}
                                sx={{padding: '0px', height: '16px', width: '16px'}}
                            />
                        </IconButton>
                    }
                    sx={{padding: '0px 8px', margin: '0px', minHeight: '24px', alignItems: 'center'}}
                >
                    <Box sx={{
                        display: 'flex',
                        alignItems: 'center',
                        overflow: 'hidden',
                        whiteSpace: 'nowrap',
                        textOverflow: 'ellipsis'
                    }}>
                        {diff.action === "add" ? (
                            <AddIcon sx={{color: 'green', marginRight: 1, fontSize: '16px'}}/>
                        ) : (
                            <RemoveIcon sx={{color: 'red', marginRight: 1, fontSize: '16px'}}/>
                        )}
                        <Box
                            component="a"
                            href={`${baseURL}${diff.url}`}
                            target="_blank"
                            rel="noopener noreferrer"
                            title={`${baseURL}${diff.url}`} // Full URL on hover
                            sx={{
                                color: 'inherit',
                                textDecoration: 'none',
                                overflow: 'hidden',
                                whiteSpace: 'nowrap',
                                textOverflow: 'ellipsis',
                                '&:hover': {
                                    color: 'inherit',
                                    textDecoration: 'underline',
                                },
                                display: 'block',
                                flex: '1 1 auto',
                                minWidth: 0
                            }}
                        >
                            <ListItemText primary={truncateTextFromStart(diff.url, 40)}
                                          sx={{padding: '0px', margin: '0px', lineHeight: '1', fontSize: '14px'}}/>
                        </Box>
                    </Box>
                </ListItem>
            ))}
            {diffToPrevCrawl.length > diffToPrevCrawlWithLimit.length &&
                <ListItem>and {diffToPrevCrawl.length - diffToPrevCrawlWithLimit.length} more...</ListItem>}
            <ListItem
                sx={{bgcolor: 'primary.main', color: 'white', padding: '2px 8px', margin: '2px 0', minHeight: '24px'}}>
                <Typography variant='body2' sx={{lineHeight: '1', fontSize: '14px'}}>{crawl.finishedAt}</Typography>
            </ListItem>
        </List>
    );
};

export default CrawlItem;
