import { useState } from "react";
import {
    Alert,
    Image,
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import * as ImagePicker from "expo-image-picker";

import { uploadImage, type UploadImageType } from "@/src/api/uploadApi";
import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import { normalizeImageUrl } from "@/src/utils/imageUrlUtils";

type ImageUploadPickerProps = Readonly<{
    label: string;
    helperText?: string;
    imageUrl?: string | null;
    imageType: UploadImageType;
    previewShape?: "circle" | "cover";
    onChangeImage: (imageUrl: string, publicId: string) => void;
}>;

export function ImageUploadPicker({
    label,
    helperText,
    imageUrl,
    imageType,
    previewShape = "cover",
    onChangeImage,
}: ImageUploadPickerProps) {
    const theme = useAppTheme();
    const colors = theme.colors;
    const [isUploading, setIsUploading] = useState(false);
    const [imageFailed, setImageFailed] = useState(false);

    const normalizedImageUrl = normalizeImageUrl(imageUrl);
    const shouldShowImage = Boolean(normalizedImageUrl) && !imageFailed;

    async function uploadPickedAsset(asset: ImagePicker.ImagePickerAsset) {
        try {
            setIsUploading(true);
            setImageFailed(false);

            const uploadedImage = await uploadImage(
                {
                    uri: asset.uri,
                    fileName: asset.fileName,
                    mimeType: asset.mimeType,
                },
                imageType
            );

            onChangeImage(uploadedImage.imageUrl, uploadedImage.publicId);
        } catch (error: unknown) {
            Alert.alert(
                "Image upload failed",
                getApiErrorMessage(error, "Please choose a different image and try again.")
            );
        } finally {
            setIsUploading(false);
        }
    }

    async function pickFromLibrary() {
        const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();

        if (!permission.granted) {
            Alert.alert(
                "Permission needed",
                "Please allow photo library access to choose an image."
            );
            return;
        }

        const result = await ImagePicker.launchImageLibraryAsync({
            mediaTypes: ["images"],
            allowsEditing: true,
            aspect: previewShape === "circle" ? [1, 1] : [16, 9],
            quality: 0.82,
        });

        if (!result.canceled && result.assets?.[0]) {
            await uploadPickedAsset(result.assets[0]);
        }
    }

    async function takePhoto() {
        const permission = await ImagePicker.requestCameraPermissionsAsync();

        if (!permission.granted) {
            Alert.alert(
                "Permission needed",
                "Please allow camera access to take a photo."
            );
            return;
        }

        const result = await ImagePicker.launchCameraAsync({
            mediaTypes: ["images"],
            allowsEditing: true,
            aspect: previewShape === "circle" ? [1, 1] : [16, 9],
            quality: 0.82,
        });

        if (!result.canceled && result.assets?.[0]) {
            await uploadPickedAsset(result.assets[0]);
        }
    }

    function handleChooseImage() {
        Alert.alert(
            label,
            "Choose how you want to add the image.",
            [
                { text: "Photo library", onPress: () => void pickFromLibrary() },
                { text: "Camera", onPress: () => void takePhoto() },
                { text: "Cancel", style: "cancel" },
            ]
        );
    }

    function handleRemoveImage() {
        setImageFailed(false);
        onChangeImage("", "");
    }

    return (
        <View style={styles.container}>
            <View style={styles.labelRow}>
                <Text style={[styles.label, { color: colors.text }]}>{label}</Text>

                {isUploading ? (
                    <Text style={[styles.uploadingText, { color: colors.textMuted }]}>Uploading...</Text>
                ) : null}
            </View>

            <Pressable
                accessibilityRole="button"
                accessibilityLabel={label}
                disabled={isUploading}
                onPress={handleChooseImage}
                style={({ pressed }) => [
                    styles.pickerCard,
                    previewShape === "circle" && styles.circlePickerCard,
                    {
                        backgroundColor: colors.surfaceSoft,
                        borderColor: colors.border,
                    },
                    pressed && styles.pressed,
                    isUploading && styles.disabled,
                ]}
            >
                {shouldShowImage ? (
                    <Image
                        source={{ uri: normalizedImageUrl as string }}
                        style={[
                            styles.previewImage,
                            previewShape === "circle" && styles.circlePreviewImage,
                        ]}
                        onError={() => setImageFailed(true)}
                    />
                ) : (
                    <View style={styles.placeholderContent}>
                        <View
                            style={[
                                styles.placeholderIconBadge,
                                { backgroundColor: colors.primarySoft },
                            ]}
                        >
                            <Ionicons
                                name={previewShape === "circle" ? "person-outline" : "image-outline"}
                                size={26}
                                color={colors.primary}
                            />
                        </View>

                        <Text style={[styles.placeholderTitle, { color: colors.text }]}>
                            Tap to upload
                        </Text>

                        <Text style={[styles.placeholderSubtitle, { color: colors.textMuted }]}>
                            Choose from phone library or camera
                        </Text>
                    </View>
                )}
            </Pressable>

            <View style={styles.actionRow}>
                <Pressable
                    accessibilityRole="button"
                    onPress={handleChooseImage}
                    disabled={isUploading}
                    style={({ pressed }) => [
                        styles.smallAction,
                        { borderColor: colors.border, backgroundColor: colors.surface },
                        pressed && styles.pressed,
                    ]}
                >
                    <Ionicons name="cloud-upload-outline" size={17} color={colors.primary} />
                    <Text style={[styles.smallActionText, { color: colors.primary }]}>
                        {normalizedImageUrl ? "Change image" : "Upload image"}
                    </Text>
                </Pressable>

                {normalizedImageUrl ? (
                    <Pressable
                        accessibilityRole="button"
                        onPress={handleRemoveImage}
                        disabled={isUploading}
                        style={({ pressed }) => [
                            styles.smallAction,
                            { borderColor: colors.border, backgroundColor: colors.surface },
                            pressed && styles.pressed,
                        ]}
                    >
                        <Ionicons name="trash-outline" size={17} color={colors.danger} />
                        <Text style={[styles.smallActionText, { color: colors.danger }]}>Remove</Text>
                    </Pressable>
                ) : null}
            </View>

            {helperText ? (
                <Text style={[styles.helperText, { color: colors.textMuted }]}>
                    {helperText}
                </Text>
            ) : null}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        gap: spacing.sm,
    },
    labelRow: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.sm,
    },
    label: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    uploadingText: {
        fontSize: typography.caption,
        fontWeight: fontWeight.semibold,
    },
    pickerCard: {
        height: 170,
        borderWidth: 1,
        borderStyle: "dashed",
        borderRadius: radius.xl,
        overflow: "hidden",
        alignItems: "center",
        justifyContent: "center",
    },
    circlePickerCard: {
        width: 132,
        height: 132,
        borderRadius: 66,
        alignSelf: "flex-start",
    },
    previewImage: {
        width: "100%",
        height: "100%",
        resizeMode: "cover",
    },
    circlePreviewImage: {
        borderRadius: 66,
    },
    placeholderContent: {
        alignItems: "center",
        justifyContent: "center",
        gap: spacing.xs,
        padding: spacing.lg,
    },
    placeholderIconBadge: {
        width: 52,
        height: 52,
        borderRadius: radius.lg,
        alignItems: "center",
        justifyContent: "center",
        marginBottom: spacing.xs,
    },
    placeholderTitle: {
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
        textAlign: "center",
    },
    placeholderSubtitle: {
        fontSize: typography.caption,
        lineHeight: 18,
        textAlign: "center",
    },
    actionRow: {
        flexDirection: "row",
        flexWrap: "wrap",
        gap: spacing.sm,
    },
    smallAction: {
        borderWidth: 1,
        borderRadius: radius.pill,
        paddingVertical: spacing.xs,
        paddingHorizontal: spacing.sm,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.xs,
    },
    smallActionText: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
    },
    helperText: {
        fontSize: typography.caption,
        lineHeight: 18,
    },
    pressed: {
        opacity: 0.86,
        transform: [{ scale: 0.99 }],
    },
    disabled: {
        opacity: 0.68,
    },
});
