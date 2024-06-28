import React, {useState} from 'react';
import {Box, Button, Checkbox, Divider, IconButton, List, ListItem, ListItemText, Typography} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';
import FileCopyIcon from '@mui/icons-material/FileCopy';
import DownloadIcon from '@mui/icons-material/Download';
import {Crawl} from "../types/Crawl.tsx";

type SiteItemProps = {
    crawl: Crawl;
    baseURL: string;
    handleCheckUrl: (crawlId: string, url: string, newState: boolean) => void;
}

const CrawlItem: React.FC<SiteItemProps> = ({crawl, baseURL, handleCheckUrl}: SiteItemProps) => {

    function truncateTextFromStart(text: string, maxLength: number) {
        if (text.length > maxLength) {
            return '...' + text.slice(-maxLength);
        }
        return text;
    }

    function formatTimestamp(timestamp: string) {
        const date = new Date(timestamp);

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');

        return `${year}-${month}-${day}, ${hours}:${minutes} Uhr`;
    }

    const handleCopyUrls = () => {
        const urlsToCopy = diffToPrevCrawl.map(diff => diff.url).join('\n');
        navigator.clipboard.writeText(urlsToCopy).then(() => {
            console.log('URLs copied to clipboard');
        }).catch(err => {
            console.error('Failed to copy URLs: ', err);
        });
    };

    const handleDownloadCsv = () => {
        const csvContent = "data:text/csv;charset=utf-8,"
            + `Action, URL, checked\n`
            + diffToPrevCrawl.map(diff => `${diff.action},${diff.url},${diff.checked}`).join('\n');
        const encodedUri = encodeURI(csvContent);
        const link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", `sitemapDiff_${formatTimestamp(crawl.finishedAt)}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    const {diffToPrevCrawl} = crawl;
    const diffLengthLimit = 20;
    const [diffToPrevCrawlToDisplay, setDiffToPrevCrawlToDisplay] = useState(diffToPrevCrawl?.slice(0, diffLengthLimit));

    return (
        <List sx={{padding: 0}}>
            {diffToPrevCrawlToDisplay?.map((diff) => (
                <ListItem
                    key={crawl.finishedAt + diff.url}
                    secondaryAction={
                        <IconButton edge="end" aria-label="mark as done" sx={{padding: '0px', minHeight: '24px'}}>
                            <Checkbox
                                checked={diff.checked || false}
                                onChange={() => handleCheckUrl(crawl.id, diff.url, !diff.checked)}
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
                            href={diff.url}
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
                            <ListItemText primary={truncateTextFromStart(diff.url, 36)}
                                          sx={{padding: '0px', margin: '0px', lineHeight: '1', fontSize: '14px'}}/>
                        </Box>
                    </Box>
                </ListItem>
            ))}
            {diffToPrevCrawl?.length > diffToPrevCrawlToDisplay?.length &&
                <ListItem onClick={() => {
                    setDiffToPrevCrawlToDisplay(diffToPrevCrawl);
                }}
                          sx={{
                              cursor: 'pointer',
                              '&:hover': {
                                  textDecoration: 'underline',
                              }
                          }}>and {diffToPrevCrawl?.length - diffToPrevCrawlToDisplay?.length} more...</ListItem>}

            {crawl.prevCrawlId && !diffToPrevCrawl?.length && //show only when there are previous crawls but there are no changes
                <ListItem sx={{
                    lineHeight: '0.5',
                }}>- no changes -</ListItem>}

            {diffToPrevCrawl?.length > 0 &&
                <>
                    <Divider/>
                    <Box sx={{display: 'flex', justifyContent: 'space-between', padding: '8px 0'}}>
                        <Button variant="text" color="primary" onClick={handleCopyUrls} startIcon={<FileCopyIcon/>}>
                            Copy URLs
                        </Button>
                        <Button variant="text" color="primary" onClick={handleDownloadCsv} startIcon={<DownloadIcon/>}>
                            Download CSV
                        </Button>
                    </Box>
                </>}

            <ListItem
                sx={{bgcolor: 'primary.main', color: 'white', padding: '2px 8px', margin: '2px 0', minHeight: '24px'}}>
                <Typography variant='body2'
                            sx={{
                                lineHeight: '1',
                                fontSize: '14px'
                            }}>{formatTimestamp(crawl.finishedAt)}</Typography>
            </ListItem>
        </List>
    );
};

export default CrawlItem;
