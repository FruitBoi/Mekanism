package mekanism.client.gui.filter;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.transporter.TItemStackFilter;
import mekanism.common.inventory.container.tile.filter.LSItemStackFilterContainer;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

public class GuiTItemStackFilter extends GuiItemStackFilter<TItemStackFilter, TileEntityLogisticalSorter, LSItemStackFilterContainer> {

    private GuiTextField minField;
    private GuiTextField maxField;

    public GuiTItemStackFilter(LSItemStackFilterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        origFilter = container.getOrigFilter();
        filter = container.getFilter();
        isNew = container.isNew();
    }

    @Override
    protected void addButtons() {
        addButton(new GuiSlot(SlotType.NORMAL, this, 11, 18).setRenderHover(true));
        addButton(new GuiSlot(SlotType.NORMAL, this, 11, 43));
        addButton(saveButton = new TranslationButton(this, getGuiLeft() + 47, getGuiTop() + 62, 60, 20, MekanismLang.BUTTON_SAVE, () -> {
            if (!filter.getItemStack().isEmpty() && !minField.getText().isEmpty() && !maxField.getText().isEmpty()) {
                int min = Integer.parseInt(minField.getText());
                int max = Integer.parseInt(maxField.getText());
                if (max >= min && max <= 64) {
                    filter.min = Integer.parseInt(minField.getText());
                    filter.max = Integer.parseInt(maxField.getText());
                    if (isNew) {
                        Mekanism.packetHandler.sendToServer(new PacketNewFilter(tile.getPos(), filter));
                    } else {
                        Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), false, origFilter, filter));
                    }
                    sendPacketToServer(ClickedTileButton.BACK_BUTTON);
                } else if (min > max) {
                    status = MekanismLang.ITEM_FILTER_MAX_LESS_THAN_MIN.translateColored(EnumColor.DARK_RED);
                    ticker = 20;
                } else { //if(max > 64 || min > 64)
                    status = MekanismLang.ITEM_FILTER_OVER_SIZED.translateColored(EnumColor.DARK_RED);
                    ticker = 20;
                }
            } else if (filter.getItemStack().isEmpty()) {
                status = MekanismLang.ITEM_FILTER_NO_ITEM.translateColored(EnumColor.DARK_RED);
                ticker = 20;
            } else if (minField.getText().isEmpty() || maxField.getText().isEmpty()) {
                status = MekanismLang.ITEM_FILTER_SIZE_MISSING.translateColored(EnumColor.DARK_RED);
                ticker = 20;
            }
        }));
        addButton(deleteButton = new TranslationButton(this, getGuiLeft() + 109, getGuiTop() + 62, 60, 20, MekanismLang.BUTTON_DELETE, () -> {
            Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), true, origFilter, null));
            sendPacketToServer(ClickedTileButton.BACK_BUTTON);
        }));
        addButton(new MekanismImageButton(this, getGuiLeft() + 5, getGuiTop() + 5, 11, 14, getButtonLocation("back"),
              () -> sendPacketToServer(isNew ? ClickedTileButton.LS_SELECT_FILTER_TYPE : ClickedTileButton.BACK_BUTTON)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 11, getGuiTop() + 62, 10, getButtonLocation("default"),
              () -> filter.allowDefault = !filter.allowDefault, getOnHover(MekanismLang.FILTER_ALLOW_DEFAULT)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 11, getGuiTop() + 72, 10, getButtonLocation("fuzzy"),
              () -> filter.fuzzyMode = !filter.fuzzyMode, getOnHover(MekanismLang.FUZZY_MODE)));
        addButton(new ColorButton(this, getGuiLeft() + 12, getGuiTop() + 44, 16, 16, () -> filter.color,
              () -> filter.color = hasShiftDown() ? null : TransporterUtils.increment(filter.color), () -> filter.color = TransporterUtils.decrement(filter.color)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 128, getGuiTop() + 44, 11, 14, getButtonLocation("silk_touch"),
              () -> filter.sizeMode = !filter.sizeMode,
              (onHover, xAxis, yAxis) -> {
                  if (tile.singleItem && filter.sizeMode) {
                      displayTooltip(MekanismLang.SIZE_MODE_CONFLICT.translate(), xAxis, yAxis);
                  } else {
                      displayTooltip(MekanismLang.SIZE_MODE.translate(), xAxis, yAxis);
                  }
              }));
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 33, 18, 93, 43, () -> {
            List<ITextComponent> list = new ArrayList<>();
            list.add(MekanismLang.STATUS.translate(status));
            list.add(MekanismLang.ITEM_FILTER_DETAILS.translate());
            if (!filter.getItemStack().isEmpty()) {
                list.add(filter.getItemStack().getDisplayName());
            }
            return list;
        }).clearFormat());
        addButton(minField = new GuiTextField(this, 149, 19, 20, 11));
        minField.setMaxStringLength(2);
        minField.setInputValidator(InputValidator.DIGIT);
        minField.setText("" + filter.min);
        addButton(maxField = new GuiTextField(this, 149, 31, 20, 11));
        maxField.setMaxStringLength(2);
        maxField.setInputValidator(InputValidator.DIGIT);
        maxField.setText("" + filter.max);
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.MIN.translate(""), 128, 20, titleTextColor());
        drawString(MekanismLang.MAX.translate(""), 128, 32, titleTextColor());
        if (tile.singleItem && filter.sizeMode) {
            drawString(MekanismLang.ITEM_FILTER_SIZE_MODE.translateColored(EnumColor.RED, OnOff.of(filter.sizeMode)), 141, 46, titleTextColor());
        } else {
            drawString(OnOff.of(filter.sizeMode).getTextComponent(), 141, 46, titleTextColor());
        }
        drawString(OnOff.of(filter.fuzzyMode).getTextComponent(), 24, 74, titleTextColor());
        drawTransporterForegroundLayer();
    }

    @Override
    protected void drawTransporterForegroundLayer() {
        drawString(OnOff.of(filter.allowDefault).getTextComponent(), 24, 64, titleTextColor());
        renderItem(filter.getItemStack(), 12, 19);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && overTypeInput(mouseX - getGuiLeft(), mouseY - getGuiTop())) {
            ItemStack stack = minecraft.player.inventory.getItemStack();
            if (!stack.isEmpty() && !hasShiftDown()) {
                filter.setItemStack(stack.copy());
                filter.getItemStack().setCount(1);
            } else if (stack.isEmpty() && hasShiftDown()) {
                filter.setItemStack(ItemStack.EMPTY);
            }
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
        return true;
    }
}