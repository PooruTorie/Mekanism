package mekanism.common.network.to_client.container.property.list;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import net.minecraft.network.FriendlyByteBuf;

public class FilterListPropertyData<FILTER extends IFilter<?>> extends ListPropertyData<FILTER> {

    public FilterListPropertyData(short property, @Nonnull List<FILTER> values) {
        super(property, ListType.FILTER, values);
    }

    public static <FILTER extends IFilter<?>> FilterListPropertyData<FILTER> read(short property, int elements, FriendlyByteBuf buffer) {
        List<FILTER> values = new ArrayList<>(elements);
        for (int i = 0; i < elements; i++) {
            //noinspection unchecked
            values.add((FILTER) BaseFilter.readFromPacket(buffer));
        }
        return new FilterListPropertyData<>(property, values);
    }

    @Override
    protected void writeListElement(FriendlyByteBuf buffer, FILTER value) {
        value.write(buffer);
    }
}