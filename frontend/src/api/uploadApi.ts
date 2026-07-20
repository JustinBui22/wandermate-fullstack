import { axiosClient } from "./axiosClient";
import type { ApiResponse } from "../types/trip";

export type UploadImageType = "profile-images" | "trip-covers";

export type ImageUploadResponse = {
    imageUrl: string;
    publicId: string;
    fileName: string;
    imageType: UploadImageType;
};

export type LocalImageAsset = {
    uri: string;
    fileName?: string | null;
    mimeType?: string | null;
};

function getFallbackFileName(imageType: UploadImageType, uri: string) {
    const extensionMatch = uri.match(/\.([a-zA-Z0-9]+)(?:\?|$)/);
    const extension = extensionMatch?.[1] ?? "jpg";
    return `${imageType}-${Date.now()}.${extension}`;
}

function getFallbackMimeType(fileName: string) {
    const lowerFileName = fileName.toLowerCase();

    if (lowerFileName.endsWith(".png")) {
        return "image/png";
    }

    if (lowerFileName.endsWith(".webp")) {
        return "image/webp";
    }

    if (lowerFileName.endsWith(".heic")) {
        return "image/heic";
    }

    if (lowerFileName.endsWith(".heif")) {
        return "image/heif";
    }

    return "image/jpeg";
}

export async function uploadImage(
    asset: LocalImageAsset,
    imageType: UploadImageType
): Promise<ImageUploadResponse> {
    const fileName = asset.fileName || getFallbackFileName(imageType, asset.uri);
    const mimeType = asset.mimeType || getFallbackMimeType(fileName);

    const formData = new FormData();
    const reactNativeFile = {
        uri: asset.uri,
        name: fileName,
        type: mimeType,
    } as unknown as Blob;
    formData.append("file", reactNativeFile);
    formData.append("imageType", imageType);

    const response = await axiosClient.post<ApiResponse<ImageUploadResponse>>(
        "/api/v1/uploads/images",
        formData,
        {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        }
    );

    return response.data.body;
}
