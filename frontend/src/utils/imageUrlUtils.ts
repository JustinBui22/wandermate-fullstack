export function normalizeImageUrl(value?: string | null) {
    const url = value?.trim();

    if (!url) {
        return null;
    }

    const googleDriveMatch = url.match(/drive\.google\.com\/file\/d\/([^/]+)/);
    if (googleDriveMatch?.[1]) {
        return `https://drive.google.com/uc?export=view&id=${googleDriveMatch[1]}`;
    }

    if (url.includes("dropbox.com")) {
        return url.replace("www.dropbox.com", "dl.dropboxusercontent.com").replace("?dl=0", "");
    }

    const imgurPageMatch = url.match(/^https?:\/\/imgur\.com\/([a-zA-Z0-9]+)$/);
    if (imgurPageMatch?.[1]) {
        return `https://i.imgur.com/${imgurPageMatch[1]}.jpg`;
    }

    return url;
}
